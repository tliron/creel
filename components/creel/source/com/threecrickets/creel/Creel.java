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
import com.threecrickets.creel.internal.Properties;
import com.threecrickets.creel.util.ArgumentsHelper;

/**
 * A simple command line interface to Creel.
 * <p>
 * Use the "--help" option to see all available options.
 * <p>
 * By default will try to load its configuration from a file named
 * "creel.properties" in the current directory.
 * 
 * @author Tal Liron
 */
public class Creel
{
	//
	// Main
	//

	public static void main( String[] arguments )
	{
		Manager manager = null;

		try
		{
			ArgumentsHelper argumentsHelper = new ArgumentsHelper( arguments );

			if( argumentsHelper.hasSwitch( "help", "h" ) )
			{
				help( System.out );
				return;
			}

			String propertiesPath = argumentsHelper.getString( "properties", "p", "creel.properties" );
			File propertiesFile = new File( propertiesPath ).getCanonicalFile();
			if( !propertiesFile.exists() )
			{
				help( System.out );
				return;
			}

			Properties properties = new Properties( propertiesFile );

			String destinationPath = properties.getProperty( "destination", "lib" );
			destinationPath = argumentsHelper.getString( "destination", "d", destinationPath );

			String statePath = properties.getProperty( "state", null );
			statePath = argumentsHelper.getString( "state", "s", statePath );

			int end = properties.getInteger( "end", 4 );
			end = argumentsHelper.getInt( "end", "e", end );

			String defaultPlatform = properties.getProperty( "platform", "maven" );
			defaultPlatform = argumentsHelper.getString( "platform", "l", defaultPlatform );

			boolean quiet = properties.getBoolean( "quiet", false );
			quiet = quiet || argumentsHelper.hasSwitch( "quiet", "q" );

			boolean ansi = properties.getBoolean( "ansi", false );
			ansi = ansi || argumentsHelper.hasSwitch( "ansi", "a" );

			boolean overwrite = properties.getBoolean( "overwrite", false );
			overwrite = overwrite || argumentsHelper.hasSwitch( "overwrite", "o" );

			boolean flat = properties.getBoolean( "flat", false );
			flat = flat || argumentsHelper.hasSwitch( "flat", "f" );

			boolean multithreaded = properties.getBoolean( "multithreaded", true );
			multithreaded = argumentsHelper.getBoolean( "multithreaded", "m", multithreaded );

			manager = new Manager();
			if( !quiet )
				( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( ansi ) );

			manager.info( "Using " + propertiesFile );

			manager.setRootDir( destinationPath );
			manager.setStateFile( statePath );
			manager.setDefaultPlatform( defaultPlatform );
			manager.setOverwrite( overwrite );
			manager.setFlat( flat );
			manager.setMultithreaded( multithreaded );
			manager.setExplicitModules( properties.getExplicitModuleConfigs() );
			manager.setRepositories( properties.getRepositoryConfigs() );
			manager.setRules( properties.getRuleConfigs() );

			if( end > 0 )
				manager.identify();

			if( end > 1 )
				manager.install();
		}
		catch( Throwable x )
		{
			if( manager != null )
				manager.error( x );
			else
				x.printStackTrace( System.err );
		}
	}

	//
	// Static operations
	//

	public static void help( PrintStream out )
	{
		out.println( "Creel is a lightweight and lightning-fast tool for resolving and downloading" );
		out.println( "JVM dependencies from Maven repositories." );
		out.println();
		out.println( "Options:" );
		out.println( "  --help, -h              Show this help" );
		out.println( "  --properties=, -p       Use properties file (default: creel.properties)" );
		out.println( "  --destination=, -d      Download to directory (default: lib)" );
		out.println( "  --state=, -s            State file (default: [destination]/.creel)" );
		out.println( "  --end=, -e              Where to end: 1=identify, 2=install, 3=unpack, 4=delete redundant (default: 4)" );
		out.println( "  --platform=, -l         Set default platform (default: maven)" );
		out.println( "  --quiet, -q             Quiet mode: don't output anything" );
		out.println( "  --ansi, -a              ANSI terminal output: pretty colors and animations" );
		out.println( "  --overwrite, -o         Overwrite files if they already exist" );
		out.println( "  --flat, -f              Flat file structure (no subdirectories)" );
		out.println( "  --multithreaded=, -m    Set multi-threaded mode (default: true)" );
		out.println();
		out.println( "For more information see: https://github.com/tliron/creel" );
	}
}
