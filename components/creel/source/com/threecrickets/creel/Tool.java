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

package com.threecrickets.creel;

import java.io.File;
import java.io.PrintStream;

import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.internal.Configuration;
import com.threecrickets.creel.util.ArgumentsHelper;

/**
 * A simple command line interface to Creel.
 * <p>
 * Use the "--help" option to see all available options.
 * <p>
 * By default will try to load its configuration from a file named
 * "creel.properties" in the current directory. You can specify all command line
 * arguments in the properties file, though explicit command line values will
 * override them.
 * <p>
 * Unless "--quiet" is activated, will use a {@link ConsoleEventHandler}.
 * 
 * @author Tal Liron
 */
public class Tool
{
	//
	// Main
	//

	public static void main( String[] arguments )
	{
		Engine engine = null;

		try
		{
			ArgumentsHelper argumentsHelper = new ArgumentsHelper( arguments );

			if( argumentsHelper.hasSwitch( "help", "h" ) )
			{
				help( System.out );
				return;
			}

			String configPath = argumentsHelper.getString( "config", "c", "creel.properties" );
			File configFile = new File( configPath ).getCanonicalFile();
			if( !configFile.exists() )
			{
				help( System.out );
				return;
			}

			Configuration configuration = new Configuration( configFile );

			String defaultPath = configuration.getProperty( "default", null );
			defaultPath = argumentsHelper.getString( "default", "d", defaultPath );

			String libraryPath = configuration.getProperty( "library", "libraries" + File.pathSeparatorChar + "jars" );
			libraryPath = argumentsHelper.getString( "library", "l", libraryPath );

			String apiPath = configuration.getProperty( "api", null );
			apiPath = argumentsHelper.getString( "api", "i", apiPath );

			String sourcePath = configuration.getProperty( "source", null );
			sourcePath = argumentsHelper.getString( "source", "s", sourcePath );

			String statePath = configuration.getProperty( "state", null );
			statePath = argumentsHelper.getString( "state", "t", statePath );

			int end = configuration.getInteger( "end", Engine.Stage.ALL.getValue() );
			end = argumentsHelper.getInteger( "end", "e", end );

			String defaultPlatform = configuration.getProperty( "platform", "mvn" );
			defaultPlatform = argumentsHelper.getString( "platform", "p", defaultPlatform );

			boolean quiet = configuration.getBoolean( "quiet", false );
			quiet = quiet || argumentsHelper.hasSwitch( "quiet", "q" );

			int verbosity = configuration.getInteger( "verbosity", 1 );
			verbosity = argumentsHelper.getInteger( "verbosity", "v", verbosity );

			boolean ansi = configuration.getBoolean( "ansi", false );
			ansi = ansi || argumentsHelper.hasSwitch( "ansi", "a" );

			boolean overwrite = configuration.getBoolean( "overwrite", false );
			overwrite = overwrite || argumentsHelper.hasSwitch( "overwrite", "w" );

			boolean flat = configuration.getBoolean( "flat", false );
			flat = flat || argumentsHelper.hasSwitch( "flat", "f" );

			boolean multithreaded = configuration.getBoolean( "multithreaded", true );
			multithreaded = argumentsHelper.getBoolean( "multithreaded", "m", multithreaded );

			engine = new Engine();
			if( !quiet )
				( (EventHandlers) engine.getEventHandler() ).add( new ConsoleEventHandler( ansi, verbosity > 1 ) );

			engine.info( "Configuration: " + configFile );

			engine.getDirectories().setDefault( defaultPath );
			engine.getDirectories().setLibrary( libraryPath );
			engine.getDirectories().setApi( apiPath );
			engine.getDirectories().setSource( sourcePath );
			engine.setStateFile( statePath );
			engine.setDefaultPlatform( defaultPlatform );
			engine.setVerbosity( verbosity );
			engine.setOverwrite( overwrite );
			engine.setFlat( flat );
			engine.setMultithreaded( multithreaded );
			engine.setExplicitModules( configuration.getModuleSpecificationConfigs() );
			engine.setRepositories( configuration.getRepositoryConfigs() );
			engine.setRules( configuration.getRuleConfigs() );

			engine.run( end );
		}
		catch( Throwable x )
		{
			if( engine != null )
				engine.error( x );
			else
				x.printStackTrace( System.err );
		}
	}

	//
	// Static operations
	//

	public static void help( PrintStream out )
	{
		out.println( "Creel " + Engine.getVersion() );
		out.println();
		out.println( "A lightweight and lightning-fast tool for resolving and downloading" );
		out.println( "JVM dependencies from Maven repositories." );
		out.println();
		out.println( "Options:" );
		out.println( "  --help, -h              Show this help" );
		out.println( "  --config=, -c           Load config file (default: creel.properties)" );
		out.println( "  --default=, -d          Unpack artifacts by default to directory (do not unpack by default)" );
		out.println( "  --library=, -l          Download library artifacts to directory (default: libraries/jars)" );
		out.println( "  --api=, -i              Download API artifacts to directory (do not download by default)" );
		out.println( "  --source=, -s           Download source artifacts to directory (do not download by default)" );
		out.println( "  --state=, -t            State file (default: [default dir]/.creel, or [current dir]/.creel if default not set)" );
		out.println( "  --end=, -e              At which stage to end: 1=identify, 2=install, 3=unpack, 4=delete redundant (default: 4)" );
		out.println( "  --platform=, -p         Set default platform (default: mvn)" );
		out.println( "  --quiet, -q             Quiet mode: don't output anything" );
		out.println( "  --verbosity=, -v        Output verbosity (default: 1)" );
		out.println( "  --ansi, -a              ANSI terminal output: pretty colors and animations" );
		out.println( "  --overwrite, -w         Overwrite files if they already exist" );
		out.println( "  --flat, -f              Flat file structure (no subdirectories)" );
		out.println( "  --multithreaded=, -m    Set multi-threaded mode (default: true)" );
		out.println();
		out.println( "For more information see: https://github.com/tliron/creel" );
	}
}
