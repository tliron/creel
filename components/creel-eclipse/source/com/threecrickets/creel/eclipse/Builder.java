/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.eclipse;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Directories;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.util.DotSeparatedStringComparator;
import com.threecrickets.creel.util.SortedProperties;

/**
 * Runs Creel to install or clean dependencies.
 * <p>
 * Builder ID: "com.threecrickets.creel.builder"
 * 
 * @author Tal Liron
 */
public class Builder extends IncrementalProjectBuilder
{
	//
	// Constants
	//

	public static final String ID = Plugin.ID + ".builder";

	public static final String CONFIGURATION_ARGUMENT = "configuration";

	public static final String DEFAULT_ARGUMENT = "default";

	public static final String LIBRARY_ARGUMENT = "library";

	public static final String API_ARGUMENT = "api";

	public static final String SOURCE_ARGUMENT = "source";

	public static final String STATE_ARGUMENT = "state";

	//
	// Static operations
	//

	public static IFile getConfigurationFile( IProject project, Map<String, String> arguments ) throws CoreException
	{
		String configuration = arguments.get( Builder.CONFIGURATION_ARGUMENT );
		return configuration != null ? getInterpolatedFile( project, configuration ) : null;
	}

	public static boolean hasDefaultConfigurationFile( IProject project ) throws CoreException
	{
		return project.getFile( "creel.properties" ).exists();
	}

	public static boolean ensureDefaultConfiguration( IProject project ) throws CoreException, IOException
	{
		IFile configurationFile = project.getFile( "creel.properties" );
		if( configurationFile.exists() )
			return false;

		Properties configuration = new SortedProperties( new DotSeparatedStringComparator<Object>() );
		configuration.setProperty( "repository.1.id", "central" );
		configuration.setProperty( "repository.1.url", "https://repo1.maven.org/maven2/" );
		configuration.setProperty( "#module.1.group", "com.github.sommeri" );
		configuration.setProperty( "#module.1.name", "less4j" );
		configuration.setProperty( "#module.1.version", "(,1.15.2)" );

		Writer writer = new StringWriter();
		configuration.store( writer, "Created by Creel " + Engine.getVersion() + " Eclipse plugin" );
		EclipseUtil.write( writer.toString(), configurationFile );

		return true;
	}

	public static Map<Artifact.Type, IContainer> getFolders( IProject project ) throws CoreException
	{
		Map<Artifact.Type, IContainer> folders = new HashMap<Artifact.Type, IContainer>();

		ICommand builder = EclipseUtil.getBuilder( project, Builder.ID );
		if( builder != null )
		{
			Map<String, String> arguments = builder.getArguments();

			String theDefault = arguments.get( Builder.DEFAULT_ARGUMENT );
			if( theDefault != null )
				folders.put( null, getInterpolatedFolder( project, theDefault ) );

			String library = arguments.get( Builder.LIBRARY_ARGUMENT );
			if( library != null )
				folders.put( Artifact.Type.LIBRARY, getInterpolatedFolder( project, library ) );

			String api = arguments.get( Builder.API_ARGUMENT );
			if( api != null )
				folders.put( Artifact.Type.API, getInterpolatedFolder( project, api ) );

			String source = arguments.get( Builder.SOURCE_ARGUMENT );
			if( source != null )
				folders.put( Artifact.Type.SOURCE, getInterpolatedFolder( project, source ) );
		}

		return folders;
	}

	public static Directories toDirectories( Map<Artifact.Type, IContainer> folders ) throws CoreException
	{
		Directories directories = new Directories();
		for( Map.Entry<Artifact.Type, IContainer> entry : folders.entrySet() )
			try
			{
				directories.set( entry.getKey(), entry.getValue().getLocation().toFile() );
			}
			catch( IOException x )
			{
				throw new CoreException( Status.CANCEL_STATUS );
			}
		return directories;
	}

	//
	// IncrementalProjectBuilder
	//

	protected IProject[] build( int kind, Map<String, String> arguments, IProgressMonitor progressMonitor ) throws CoreException
	{
		String configuruation = arguments.get( Builder.CONFIGURATION_ARGUMENT );
		if( configuruation == null )
			// Not using a configuration file, nothing to do
			return null;

		IProject project = getProject();
		IFile configurationFile = getConfigurationFile( project, arguments );

		IResourceDelta delta = getDelta( project );
		if( delta != null ) // is null when cleaning
		{
			IResourceDelta configurationFileDelta = delta.findMember( configurationFile.getProjectRelativePath() );
			if( ( configurationFileDelta == null ) || ( configurationFileDelta.getKind() == IResourceDelta.NO_CHANGE ) )
				// No change, nothing to do
				return null;
		}

		if( !configurationFile.exists() )
			// No configuration file, nothing to do
			return null;

		// Run Creel
		Map<Artifact.Type, IContainer> folders = getFolders( project );
		Engine engine = new Engine();
		( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( false, true ) );
		engine.getDirectories().set( toDirectories( folders ) );
		try
		{
			engine.loadConfiguration( configurationFile.getRawLocation().toFile() );
		}
		catch( IOException x )
		{
			throw new CoreException( Status.CANCEL_STATUS );
		}
		engine.run();

		if( project.hasNature( JavaCore.NATURE_ID ) )
		{
			// Update classpath
			IJavaProject javaProject = JavaCore.create( project );
			EclipseUtil.setClasspathContainer( javaProject, new Classpath( project ) );
		}

		// Update folders
		for( IContainer folder : folders.values() )
			folder.refreshLocal( IResource.DEPTH_INFINITE, null );

		return null;
	}

	@Override
	protected void clean( IProgressMonitor monitor )
	{
		IProject project = getProject();

		try
		{
			Map<Artifact.Type, IContainer> folders = getFolders( project );

			// Clean
			Engine engine = new Engine();
			( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( false, true ) );
			engine.getDirectories().set( toDirectories( folders ) );
			engine.load();
			engine.clean();

			if( project.hasNature( JavaCore.NATURE_ID ) )
			{
				// Update classpath
				IJavaProject javaProject = JavaCore.create( project );
				EclipseUtil.setClasspathContainer( javaProject, new Classpath( project ) );
			}

			// Update folders
			for( IContainer folder : folders.values() )
				folder.refreshLocal( IResource.DEPTH_INFINITE, null );
		}
		catch( CoreException x )
		{
			throw new RuntimeException( x );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static IFile getInterpolatedFile( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		return project.getFile( name );
	}

	private static IContainer getInterpolatedFolder( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		// Note: project.getFolder cannot get the root! (?)
		return name.isEmpty() ? project : project.getFolder( name );
	}
}
