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
import com.threecrickets.creel.Manager;
import com.threecrickets.creel.ant.internal.DynamicType;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.internal.Properties;

/**
 * build.xml:
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="Sincerity" default="compile" xmlns:creel="antlib:com.threecrickets.creel.ant"&gt;
 * 	&lt;taskdef uri="antlib:com.threecrickets.creel.ant" resource="com/threecrickets/creel/ant/antlib.xml" classpath="creel.jar"/&gt;
 *  &lt;target name="dependencies&gt;
 * 	  &lt;creel:dependencies conflictPolicy="newest" destDir="lib" pathid="my.dependencies.classpath"&gt;
 * 	    &lt;module group="com.github.sommeri" name="less4j" version="(,1.15.2)"/&gt;
 * 	    &lt;module group="org.jsoup" name="jsoup" version="1.8.1"/&gt;
 *      &lt;repository id="restlet" url="http://maven.restlet.com" all="false"/&gt;
 *      &lt;repository id="central" url="https://repo1.maven.org/maven2/"/&gt;
 *      &lt;rule type="exclude" name="*annotations*"/&gt;
 *    &lt;/creel:dependencies&gt;
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
 * Test.java:
 * 
 * <pre>
 * {@code
 * import org.jsoup.Jsoup;
 * public class Test {}
 * }
 * </pre>
 * 
 * @author Tal Liron
 */
public class DependenciesTask extends Task
{
	//
	// Attributes
	//

	public void setProperties( FileResource properties )
	{
		this.properties = properties;
	}

	public void setPathId( String pathId )
	{
		this.pathId = pathId;
	}

	public void setDestDir( FileResource destDir )
	{
		this.destDir = destDir;
	}

	public void setState( FileResource state )
	{
		this.state = state;
	}

	public void setOverwrite( boolean overwrite )
	{
		this.overwrite = overwrite;
	}

	public void setFlat( boolean flat )
	{
		this.flat = flat;
	}

	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	public void setConflictPolicy( String conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	public void setMultithreaded( boolean multithreaded )
	{
		this.multithreaded = multithreaded;
	}

	public void setQuiet( boolean quiet )
	{
		this.quiet = quiet;
	}

	//
	// Operations
	//

	public DynamicType createModule()
	{
		DynamicType config = new DynamicType();
		modules.add( config );
		return config;
	}

	public DynamicType createRepository()
	{
		DynamicType config = new DynamicType();
		repositories.add( config );
		return config;
	}

	public DynamicType createRule()
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
		Manager manager = new Manager();

		if( defaultPlatform != null )
			manager.setDefaultPlatform( defaultPlatform );

		if( conflictPolicy != null )
			manager.setConflictPolicy( conflictPolicy );

		manager.setMultithreaded( multithreaded );

		if( !quiet )
			( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( false ) );

		if( properties != null )
		{
			try
			{
				Properties properties = new Properties( this.properties.getFile() );
				modules = properties.getExplicitModuleConfigs();
				repositories = properties.getRepositoryConfigs();
			}
			catch( IOException x )
			{
				throw new BuildException( "Could not load properties: " + properties.getFile(), x );
			}
		}

		try
		{
			manager.setRootDir( destDir.getFile() );
			if( state != null )
				manager.setStateFile( state.getFile() );
		}
		catch( IOException x )
		{
			throw new BuildException( x );
		}
		manager.setOverwrite( overwrite );
		manager.setFlat( flat );
		manager.setExplicitModules( modules );
		manager.setRepositories( repositories );
		manager.setRules( rules );

		manager.identify();

		Iterable<Artifact> artifacts = manager.install();

		if( pathId != null )
		{
			Path path = new Path( getProject() );
			for( Artifact artifact : artifacts )
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

	private String defaultPlatform;

	private String conflictPolicy;

	private boolean multithreaded = true;

	private boolean quiet;
}
