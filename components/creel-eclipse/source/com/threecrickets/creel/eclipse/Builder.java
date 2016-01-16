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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Directories;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.util.DotSeparatedStringComparator;
import com.threecrickets.creel.util.IoUtil;
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

	public static final String SCRIPT_FILE_ARGUMENT = "script";

	public static final String SCRIPT_ENGINE_ARGUMENT = "scriptEngine";

	public static final String CONFIGURATION_FILE_ARGUMENT = "configuration";

	public static final String DEFAULT_DIR_ARGUMENT = "default";

	public static final String LIBRARY_DIR_ARGUMENT = "library";

	public static final String API_DIR_ARGUMENT = "api";

	public static final String SOURCE_DIR_ARGUMENT = "source";

	public static final String STATE_FILE_ARGUMENT = "state";

	//
	// Static operations
	//

	/**
	 * Creates default arguments for the builder according to the project
	 * structure.
	 * 
	 * @param project
	 *        The project
	 * @return The default arguments
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static Map<String, String> getDefaultArguments( IProject project ) throws CoreException
	{
		Map<String, String> arguments = new HashMap<String, String>();
		if( hasDefaultScriptFile( project ) )
		{
			arguments.put( SCRIPT_FILE_ARGUMENT, "creel.js" );
			arguments.put( SCRIPT_ENGINE_ARGUMENT, "JavaScript" );
		}
		else if( hasDefaultConfigurationFile( project ) )
			arguments.put( CONFIGURATION_FILE_ARGUMENT, "creel.properties" );
		arguments.put( DEFAULT_DIR_ARGUMENT, "." );
		arguments.put( LIBRARY_DIR_ARGUMENT, "libraries/jars" );
		arguments.put( API_DIR_ARGUMENT, "reference/api" );
		arguments.put( SOURCE_DIR_ARGUMENT, "reference/source" );
		return arguments;
	}

	/**
	 * Retrieves and interpolates the "script" builder argument.
	 * 
	 * @param project
	 *        The project
	 * @param arguments
	 *        The builder arguments
	 * @return The script file or null
	 * @throws CoreException
	 */
	public static IFile getScriptFile( IProject project, Map<String, String> arguments ) throws CoreException
	{
		String script = arguments.get( SCRIPT_FILE_ARGUMENT );
		return script != null ? EclipseUtil.getInterpolatedFile( project, script ) : null;
	}

	/**
	 * Checks whether a "creel.js" file exists in the default location.
	 * 
	 * @param project
	 *        The project
	 * @return True if exists
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static boolean hasDefaultScriptFile( IProject project ) throws CoreException
	{
		return project.getFile( "creel.js" ).exists();
	}

	/**
	 * Makes sure a "creel.js" file exists in the default location. If it's not
	 * there, creates an example file.
	 * 
	 * @param project
	 *        The project
	 * @return True if an example file was created, false if file already exists
	 * @throws CoreException
	 *         In case of an Eclipse error
	 * @throws IOException
	 *         In case of an I/O error
	 */
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
		writer.println( "//engine.modules = [" );
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

	/**
	 * Retrieves and interpolates the "configuration" builder argument.
	 * 
	 * @param project
	 *        The project
	 * @param arguments
	 *        The builder arguments
	 * @return The configuration file or null
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static IFile getConfigurationFile( IProject project, Map<String, String> arguments ) throws CoreException
	{
		String configuration = arguments.get( CONFIGURATION_FILE_ARGUMENT );
		return configuration != null ? EclipseUtil.getInterpolatedFile( project, configuration ) : null;
	}

	/**
	 * Checks whether a "creel.properties" file exists in the default location.
	 * 
	 * @param project
	 *        The project
	 * @return True if exists
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static boolean hasDefaultConfigurationFile( IProject project ) throws CoreException
	{
		return project.getFile( "creel.properties" ).exists();
	}

	/**
	 * Makes sure a "creel.properties" file exists in the default location. If
	 * it's not there, creates an example file.
	 * 
	 * @param project
	 *        The project
	 * @return True if an example file was created, false if file already exists
	 * @throws CoreException
	 *         In case of an Eclipse error
	 * @throws IOException
	 *         In case of an I/O error
	 */
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

	/**
	 * Retrieves and interpolates the folder builder arguments.
	 * 
	 * @param project
	 *        The project
	 * @return Map of artifact types to folders
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static Map<Artifact.Type, IContainer> getFolders( IProject project ) throws CoreException
	{
		Map<Artifact.Type, IContainer> folders = new HashMap<Artifact.Type, IContainer>();

		ICommand builder = EclipseUtil.getBuilder( project, ID );
		if( builder != null )
		{
			Map<String, String> arguments = builder.getArguments();

			String theDefault = arguments.get( DEFAULT_DIR_ARGUMENT );
			if( theDefault != null )
			{
				IContainer folder = EclipseUtil.getInterpolatedFolder( project, theDefault );
				if( folder != null )
					folders.put( null, folder );
			}

			String library = arguments.get( LIBRARY_DIR_ARGUMENT );
			if( library != null )
			{
				IContainer folder = EclipseUtil.getInterpolatedFolder( project, library );
				if( folder != null )
					folders.put( Artifact.Type.LIBRARY, folder );
			}

			String api = arguments.get( API_DIR_ARGUMENT );
			if( api != null )
			{
				IContainer folder = EclipseUtil.getInterpolatedFolder( project, api );
				if( folder != null )
					folders.put( Artifact.Type.API, folder );
			}

			String source = arguments.get( SOURCE_DIR_ARGUMENT );
			if( source != null )
			{
				IContainer folder = EclipseUtil.getInterpolatedFolder( project, source );
				if( folder != null )
					folders.put( Artifact.Type.SOURCE, folder );
			}
		}

		return folders;
	}

	/**
	 * Translate Eclipse folders to a Creel directories instance.
	 * 
	 * @param folders
	 *        Map of artifact types to folders
	 * @return Creel directories
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
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

	/**
	 * Creates and configures a Creel engine using plugin preferences and
	 * builder arguments.
	 * 
	 * @param project
	 *        The project
	 * @return A Creel engine
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static Engine createEngine( IProject project ) throws CoreException
	{
		return createEngine( project, getFolders( project ) );
	}

	/**
	 * Creates and configures a Creel engine using plugin preferences and
	 * folders.
	 * 
	 * @param project
	 *        The project
	 * @param folders
	 *        Map of artifact types to folders
	 * @return A Creel engine
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static Engine createEngine( IProject project, Map<Artifact.Type, IContainer> folders ) throws CoreException
	{
		IPreferenceStore preferences = Plugin.instance.getPreferenceStore();

		Engine engine = new Engine();
		IOConsole console = EclipseUtil.getConsole( "Creel" );
		IOConsoleOutputStream stream = console.newOutputStream();
		if( !preferences.getBoolean( PreferencesPage.QUIET ) )
			( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( stream, false, false ) );
		engine.setVerbosity( preferences.getInt( PreferencesPage.VERBOSITY ) );
		engine.getDirectories().set( toDirectories( folders ) );

		return engine;
	}

	//
	// IncrementalProjectBuilder
	//

	protected IProject[] build( int kind, Map<String, String> arguments, IProgressMonitor progressMonitor ) throws CoreException
	{
		String script = arguments.get( SCRIPT_FILE_ARGUMENT );
		String configuration = arguments.get( CONFIGURATION_FILE_ARGUMENT );
		if( ( script == null ) && ( configuration == null ) )
			// Not using a script or configuration file, nothing to do
			return null;

		IProject project = getProject();
		IFile scriptFile = null, configurationFile = null;
		ScriptEngine scriptEngine = null;

		if( script != null )
		{
			scriptFile = getScriptFile( project, arguments );
			if( ( scriptFile != null ) && scriptFile.exists() )
			{
				String scriptEngineName = arguments.get( SCRIPT_ENGINE_ARGUMENT );
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
			configurationFile = getConfigurationFile( project, arguments );
			if( ( configurationFile != null ) && !configurationFile.exists() )
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
		Map<Artifact.Type, IContainer> folders = getFolders( project );
		Engine engine = createEngine( project, folders );

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
			Map<Artifact.Type, IContainer> folders = getFolders( project );

			// Clean
			Engine engine = createEngine( project );
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
