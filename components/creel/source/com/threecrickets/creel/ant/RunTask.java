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

package com.threecrickets.creel.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.Engine.ConflictPolicy;
import com.threecrickets.creel.Tool;
import com.threecrickets.creel.ant.internal.DynamicType;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.internal.Properties;

/**
 * <a href="http://ant.apache.org/">Ant</a> task for Creel. Allows you to
 * download, install, and upgrade dependencies, as well as include them in the
 * classpath for compilation.
 * <p>
 * An example build.xml:
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="Sincerity" default="compile" xmlns:creel="antlib:com.threecrickets.creel.ant"&gt;
 * 	&lt;taskdef uri="antlib:com.threecrickets.creel.ant" resource="com/threecrickets/creel/ant/antlib.xml" classpath="creel.jar"/&gt;
 *  &lt;target name="dependencies&gt;
 * 	  &lt;creel:run conflictPolicy="newest" destDir="lib" pathId="my.dependencies.classpath"&gt;
 * 	    &lt;module group="com.github.sommeri" name="less4j" version="(,1.15.2)"/&gt;
 * 	    &lt;module group="org.jsoup" name="jsoup" version="1.8.1"/&gt;
 *      &lt;repository id="restlet" url="http://maven.restlet.com" all="false"/&gt;
 *      &lt;repository id="central" url="https://repo1.maven.org/maven2/"/&gt;
 *      &lt;rule type="exclude" name="*annotations*"/&gt;
 *    &lt;/creel:run&gt;
 *    &lt;echo&gt;${toString:my.dependencies.classpath}&lt;/echo&gt;
 *  &lt;/target&gt;
 *  &lt;target name="compile" depends="dependencies"&gt;
 *    &lt;javac srcdir="." classpathref="my.dependencies.classpath"&gt;
 *      &lt;include name="Test.java"/&gt;
 * 	  &lt;/javac&gt;
 *   &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 * 
 * The example Test.java referenced above:
 * 
 * <pre>
 * {@code
 * import org.jsoup.Jsoup;
 * public class Test {}
 * }
 * </pre>
 * 
 * Note that in addition to specifying modules, repositories, and rules in
 * build.xml, you can also use the same JVM properties file format used by the
 * command line tool, {@link Tool}, by setting the "properties" attribute.
 * 
 * @author Tal Liron
 */
public class RunTask extends Task
{
	//
	// Attributes
	//

	/**
	 * The JVM properties file. If specified, will load the configuration from
	 * this file.
	 * 
	 * @param properties
	 *        The JVM properties file
	 */
	public void setProperties( FileResource properties )
	{
		this.properties = properties;
	}

	/**
	 * The path ID. If specified, will set an Ant path reference to the
	 * installed artifacts.
	 * 
	 * @param pathId
	 *        The path ID
	 */
	public void setPathId( String pathId )
	{
		this.pathId = pathId;
	}

	/**
	 * The destination root directory.
	 * 
	 * @param destDir
	 *        The destination directory
	 */
	public void setDestDir( FileResource destDir )
	{
		this.destDir = destDir;
	}

	/**
	 * Where to store state. Will default to a file named ".creel" in the root
	 * directory.
	 * 
	 * @param state
	 *        The state file
	 */
	public void setState( FileResource state )
	{
		this.state = state;
	}

	/**
	 * Whether we should overwrite files. Defaults to false.
	 * 
	 * @param overwrite
	 *        True to overwrite
	 */
	public void setOverwrite( boolean overwrite )
	{
		this.overwrite = overwrite;
	}

	/**
	 * Whether we should use a flat file structure under the root directory (no
	 * sub-directories). Defaults to false.
	 * 
	 * @param flat
	 *        True if flat
	 */
	public void setFlat( boolean flat )
	{
		this.flat = flat;
	}

	/**
	 * The default platform to use if none is specified. Defaults to "mvn".
	 * 
	 * @param defaultPlatform
	 *        The default platform name
	 */
	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	/**
	 * The module conflict resolution policy. Defaults to
	 * {@link ConflictPolicy#NEWEST}.
	 * 
	 * @param conflictPolicy
	 *        The conflict policy: "newest" or "oldest"
	 */
	public void setConflictPolicy( String conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	/**
	 * Whether we should use multiple threads when running. Defaults to true.
	 * 
	 * @param multithreaded
	 *        True if multithreaded
	 */
	public void setMultithreaded( boolean multithreaded )
	{
		this.multithreaded = multithreaded;
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
	 * The verbosity level. A higher number means more verbose. Defaults to 1.
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

	/**
	 * Creates a nested module.
	 * 
	 * @return The module
	 */
	public Map<String, String> createModule()
	{
		DynamicType config = new DynamicType();
		modules.add( config );
		return config;
	}

	/**
	 * Creates a nested repository.
	 * 
	 * @return The repository
	 */
	public Map<String, String> createRepository()
	{
		DynamicType config = new DynamicType();
		repositories.add( config );
		return config;
	}

	/**
	 * Creates a nested rule.
	 * 
	 * @return The rule
	 */
	public Map<String, String> createRule()
	{
		DynamicType config = new DynamicType();
		rules.add( config );
		return config;
	}

	//
	// Task
	//

	@Override
	public void execute()
	{
		Engine engine = new Engine();

		if( !quiet )
			( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( false, verbosity > 1 ) );

		if( properties != null )
		{
			try
			{
				Properties properties = new Properties( this.properties.getFile() );
				modules = properties.getModuleSpecificationConfigs();
				repositories = properties.getRepositoryConfigs();
			}
			catch( IOException x )
			{
				throw new BuildException( "Could not load properties: " + properties.getFile(), x );
			}
		}

		try
		{
			engine.setRootDir( destDir.getFile() );
			if( state != null )
				engine.setStateFile( state.getFile() );
		}
		catch( IOException x )
		{
			throw new BuildException( x );
		}

		engine.setDefaultPlatform( defaultPlatform );
		if( conflictPolicy != null )
			engine.setConflictPolicy( conflictPolicy );
		engine.setMultithreaded( multithreaded );
		engine.setOverwrite( overwrite );
		engine.setFlat( flat );
		engine.setVerbosity( verbosity );
		engine.setExplicitModules( modules );
		engine.setRepositories( repositories );
		engine.setRules( rules );

		engine.run();

		if( pathId != null )
		{
			Path path = new Path( getProject() );
			for( Artifact artifact : engine.getInstalledArtifacts() )
				path.createPathElement().setLocation( artifact.getFile() );
			getProject().getReferences().put( pathId, path );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Collection<Map<String, ?>> modules = new ArrayList<Map<String, ?>>();

	private Collection<Map<String, ?>> repositories = new ArrayList<Map<String, ?>>();

	private Collection<Map<String, ?>> rules = new ArrayList<Map<String, ?>>();

	private FileResource properties;

	private String pathId;

	private FileResource destDir = new FileResource( new File( "lib" ) );

	private FileResource state = null;

	private boolean overwrite;

	private boolean flat;

	private String defaultPlatform = "mvn";

	private String conflictPolicy;

	private boolean multithreaded = true;

	private boolean quiet;

	private int verbosity = 1;
}
