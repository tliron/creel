/**
 * Copyright 2015-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.resources.FileResource;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;

/**
 * <a href="http://ant.apache.org/">Ant</a> task for Creel. Allows you to delete
 * the installed artifacts and state file.
 * <p>
 * An example build.xml:
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="Sincerity" default="compile" xmlns:creel="antlib:com.threecrickets.creel.ant"&gt;
 * 	&lt;taskdef uri="antlib:com.threecrickets.creel.ant" resource="com/threecrickets/creel/ant/antlib.xml" classpath="creel.jar"/&gt;
 *  &lt;target name="clean"&gt;
 * 	  &lt;creel:clean libraryDir="lib" /&gt;
 *  &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 * 
 * @author Tal Liron
 */
public class CleanTask extends Task
{
	//
	// Attributes
	//

	/**
	 * The default directory in which to install artifacts. When null, will not
	 * install them.
	 * 
	 * @param defaultDir
	 *        The other directory
	 */
	public void setDefaultDir( FileResource defaultDir )
	{
		this.defaultDir = defaultDir;
	}

	/**
	 * The directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them. Defaults to
	 * "libraries/jars".
	 * 
	 * @param libraryDir
	 *        The library directory
	 */
	public void setLibraryDir( FileResource libraryDir )
	{
		this.libraryDir = libraryDir;
	}

	/**
	 * The directory in which to install {@link Artifact.Type#API} artifacts.
	 * When null, will not install them.
	 * 
	 * @param apiDir
	 *        The API directory
	 */
	public void setApiDir( FileResource apiDir )
	{
		this.apiDir = apiDir;
	}

	/**
	 * The directory in which to install {@link Artifact.Type#SOURCE} artifacts.
	 * When null, will not install them.
	 * 
	 * @param sourceDir
	 *        The source directory
	 */
	public void setSourceDir( FileResource sourceDir )
	{
		this.sourceDir = sourceDir;
	}

	/**
	 * Where to store state. Will default to a file named ".creel" in the other
	 * artifact directory, or the current directory if the other artifact
	 * directory was not set.
	 * 
	 * @param state
	 *        The state file
	 */
	public void setState( FileResource state )
	{
		this.state = state;
	}

	/**
	 * Set to true to disable all notifications.
	 * 
	 * @param quiet
	 *        Whether we should be quiet
	 */
	public void setQuiet( boolean quiet )
	{
		this.quiet = quiet;
	}

	/**
	 * The verbosity level. A higher number means more verbose. Defaults to 0.
	 * 
	 * @param verbosity
	 *        The verbosity level
	 */
	public void setVerbosity( int verbosity )
	{
		this.verbosity = verbosity;
	}

	//
	// Operations
	//

	//
	// Task
	//

	@Override
	public void execute()
	{
		Engine engine = new Engine();

		if( !quiet )
			( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( false, verbosity > 1 ) );

		try
		{
			if( defaultDir != null )
				engine.getDirectories().setDefault( defaultDir.getFile() );
			if( libraryDir != null )
				engine.getDirectories().setLibrary( libraryDir.getFile() );
			if( apiDir != null )
				engine.getDirectories().setApi( apiDir.getFile() );
			if( sourceDir != null )
				engine.getDirectories().setSource( sourceDir.getFile() );
			if( state != null )
				engine.setStateFile( state.getFile() );

			if( !engine.getStateFile().exists() )
				return;

			engine.setVerbosity( verbosity );
			engine.load();
			engine.clean();
		}
		catch( IOException x )
		{
			throw new BuildException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private FileResource libraryDir = new FileResource( new File( new File( "libraries" ), "jars" ) );

	private FileResource apiDir = null;

	private FileResource sourceDir = null;

	private FileResource defaultDir = null;

	private FileResource state = null;

	private boolean quiet;

	private int verbosity = 0;
}
