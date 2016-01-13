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

package com.threecrickets.creel.eclipse.internal;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Directories;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.eclipse.Builder;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.util.DotSeparatedStringComparator;
import com.threecrickets.creel.util.SortedProperties;

/**
 * Creel configuration utilities.
 * 
 * @author Tal Liron
 */
public abstract class ConfigurationUtil
{
	//
	// Static operations
	//

	public static IFile getScriptFile( IProject project, Map<String, String> arguments ) throws CoreException
	{
		String script = arguments.get( Builder.SCRIPT_ARGUMENT );
		return script != null ? EclipseUtil.getInterpolatedFile( project, script ) : null;
	}

	public static boolean hasDefaultScriptFile( IProject project ) throws CoreException
	{
		return project.getFile( "creel.js" ).exists();
	}

	public static boolean ensureDefaultScript( IProject project ) throws CoreException, IOException
	{
		IFile scriptFile = project.getFile( "creel.js" );
		if( scriptFile.exists() )
			return false;

		StringWriter content = new StringWriter();
		PrintWriter writer = new PrintWriter( content );
		writer.println( "// Originally created by Creel " + Engine.getVersion() + " Eclipse plugin" );
		writer.println();
		writer.println( "var standalone = (typeof engine === 'undefined')" );
		writer.println();
		writer.println( "if (standalone) {" );
		writer.println( "	engine = new com.threecrickets.creel.Engine()" );
		writer.println( "	engine.eventHandler.add(new com.threecrickets.creel.event.ConsoleEventHandler(true, false))" );
		writer.println( "	engine.directories.default = '.'" );
		writer.println( "	engine.directories.library = 'libraries/jars'" );
		writer.println( "	engine.directories.api = 'reference/api'" );
		writer.println( "	engine.directories.source = 'reference/source'" );
		writer.println( "}" );
		writer.println();
		writer.println( "//engine.explicitModules = [" );
		writer.println( "//	{id: 'org.jsoup:jsoup'}]" );
		writer.println();
		writer.println( "engine.repositories = [" );
		writer.println( "	{id: 'central', url: 'https://repo1.maven.org/maven2/'}]" );
		writer.println();
		writer.println( "if (standalone) {" );
		writer.println( "	engine.run()" );
		writer.println( "}" );

		EclipseUtil.write( content.toString(), scriptFile );
		return true;
	}

	public static IFile getConfigurationFile( IProject project, Map<String, String> arguments ) throws CoreException
	{
		String configuration = arguments.get( Builder.CONFIGURATION_ARGUMENT );
		return configuration != null ? EclipseUtil.getInterpolatedFile( project, configuration ) : null;
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
		configuration.setProperty( "#module.1.id", "org.jsoup:jsoup" );

		Writer writer = new StringWriter();
		configuration.store( writer, "Originally created by Creel " + Engine.getVersion() + " Eclipse plugin" );

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
				folders.put( null, EclipseUtil.getInterpolatedFolder( project, theDefault ) );

			String library = arguments.get( Builder.LIBRARY_ARGUMENT );
			if( library != null )
				folders.put( Artifact.Type.LIBRARY, EclipseUtil.getInterpolatedFolder( project, library ) );

			String api = arguments.get( Builder.API_ARGUMENT );
			if( api != null )
				folders.put( Artifact.Type.API, EclipseUtil.getInterpolatedFolder( project, api ) );

			String source = arguments.get( Builder.SOURCE_ARGUMENT );
			if( source != null )
				folders.put( Artifact.Type.SOURCE, EclipseUtil.getInterpolatedFolder( project, source ) );
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

	public static Engine createEngine( IProject project ) throws CoreException
	{
		return createEngine( project, getFolders( project ) );
	}

	public static Engine createEngine( IProject project, Map<Artifact.Type, IContainer> folders ) throws CoreException
	{
		Engine engine = new Engine();
		IOConsole console = EclipseUtil.getConsole( "Creel" );
		IOConsoleOutputStream stream = console.newOutputStream();
		( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( stream, false, false ) );
		engine.getDirectories().set( toDirectories( folders ) );
		return engine;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ConfigurationUtil()
	{
	}
}
