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
import java.io.InputStream;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.eclipse.internal.ConfigurationUtil;
import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.util.IoUtil;

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

	public static final String SCRIPT_ARGUMENT = "script";

	public static final String SCRIPT_ENGINE_ARGUMENT = "scriptEngine";

	public static final String CONFIGURATION_ARGUMENT = "configuration";

	public static final String DEFAULT_ARGUMENT = "default";

	public static final String LIBRARY_ARGUMENT = "library";

	public static final String API_ARGUMENT = "api";

	public static final String SOURCE_ARGUMENT = "source";

	public static final String STATE_ARGUMENT = "state";

	//
	// IncrementalProjectBuilder
	//

	protected IProject[] build( int kind, Map<String, String> arguments, IProgressMonitor progressMonitor ) throws CoreException
	{
		String script = arguments.get( Builder.SCRIPT_ARGUMENT );
		String configuration = arguments.get( Builder.CONFIGURATION_ARGUMENT );
		if( ( script == null ) && ( configuration == null ) )
			// Not using a script or configuration file, nothing to do
			return null;

		IProject project = getProject();
		IFile scriptFile = null, configurationFile = null;
		ScriptEngine scriptEngine = null;

		if( script != null )
		{
			scriptFile = ConfigurationUtil.getScriptFile( project, arguments );
			if( scriptFile.exists() )
			{
				String scriptEngineName = arguments.get( Builder.SCRIPT_ENGINE_ARGUMENT );
				if( scriptEngineName == null )
					scriptEngineName = "JavaScript";
				scriptEngine = new ScriptEngineManager().getEngineByName( scriptEngineName );
				if( scriptEngine == null )
					throw new CoreException( Status.CANCEL_STATUS );
			}
			else
				scriptFile = null;
		}

		if( configuration != null )
		{
			configurationFile = ConfigurationUtil.getConfigurationFile( project, arguments );
			if( !configurationFile.exists() )
				configurationFile = null;
		}

		if( ( scriptFile == null ) && ( configurationFile == null ) )
			// No script and no configuration file, nothing to do
			return null;

		IResourceDelta delta = getDelta( project );
		if( delta != null ) // is null when cleaning
		{
			boolean scriptChanged = false, configurationChanged = false;

			if( scriptFile != null )
			{
				scriptChanged = true;
				IResourceDelta scriptFileDelta = delta.findMember( scriptFile.getProjectRelativePath() );
				if( ( scriptFileDelta == null ) || ( scriptFileDelta.getKind() == IResourceDelta.NO_CHANGE ) )
					scriptChanged = false;
			}

			if( configurationFile != null )
			{
				configurationChanged = true;
				IResourceDelta configurationFileDelta = delta.findMember( configurationFile.getProjectRelativePath() );
				if( ( configurationFileDelta == null ) || ( configurationFileDelta.getKind() == IResourceDelta.NO_CHANGE ) )
					configurationChanged = false;
			}

			if( !scriptChanged && !configurationChanged )
				// No change, nothing to do
				return null;
		}

		// Run Creel
		Map<Artifact.Type, IContainer> folders = ConfigurationUtil.getFolders( project );
		Engine engine = ConfigurationUtil.createEngine( project, folders );

		try
		{
			if( scriptFile != null )
			{
				String scriptText;
				InputStream stream = scriptFile.getContents();
				try
				{
					scriptText = IoUtil.readText( scriptFile.getContents(), null );
				}
				finally
				{
					stream.close();
				}
				scriptEngine.put( "engine", engine );
				scriptEngine.eval( scriptText );
			}
			else
			{
				engine.loadConfiguration( configurationFile.getRawLocation().toFile() );
			}
		}
		catch( IOException x )
		{
			engine.error( x );
			throw new CoreException( Status.CANCEL_STATUS );
		}
		catch( ScriptException x )
		{
			engine.error( x );
			throw new CoreException( Status.CANCEL_STATUS );
		}

		try
		{
			engine.run();
		}
		catch( CreelException x )
		{
			engine.error( x );
			throw new CoreException( Status.CANCEL_STATUS );
		}

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
			Map<Artifact.Type, IContainer> folders = ConfigurationUtil.getFolders( project );

			// Clean
			Engine engine = ConfigurationUtil.createEngine( project );
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
}
