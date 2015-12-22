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

import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.internal.Properties;
import com.threecrickets.creel.util.ArgumentsHelper;

/**
 * @author Tal Liron
 */
public class Creel
{
	//
	// Main
	//

	public static void main( String[] arguments )
	{
		ArgumentsHelper argumentsHelper = new ArgumentsHelper( arguments );

		if( argumentsHelper.hasSwitch( "help", "h" ) )
		{
			help();
			return;
		}

		String propertiesPath = argumentsHelper.getString( "properties", "p", "creel.properties" );
		String destinationPath = argumentsHelper.getString( "destination", "d", "lib" );
		String databasePath = argumentsHelper.getString( "database", "b", null );
		String defaultPlatform = argumentsHelper.getString( "platform", "l", "maven" );
		boolean quiet = argumentsHelper.hasSwitch( "quiet", "q" );
		boolean ansi = argumentsHelper.hasSwitch( "ansi", "a" );
		boolean overwrite = argumentsHelper.hasSwitch( "overwrite", "o" );
		boolean flat = argumentsHelper.hasSwitch( "flat", "f" );
		boolean multithreaded = argumentsHelper.getBoolean( "multithreaded", "m", true );

		try
		{
			Properties properties = new Properties( new File( propertiesPath ) );

			Manager manager = new Manager();
			manager.setMultithreaded( multithreaded );
			manager.setDefaultPlatform( defaultPlatform );

			if( !quiet )
				( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( ansi ) );

			manager.setExplicitModules( properties.getExplicitModuleConfigs() );
			manager.setRepositories( properties.getRepositoryConfigs() );
			manager.setRules( properties.getRuleConfigs() );

			manager.identify();
			manager.install( destinationPath, databasePath, overwrite, flat );
		}
		catch( Throwable x )
		{
			x.printStackTrace();
		}
	}

	public static void help()
	{
		System.out.println( "Creel is a lightweight and lightning-fast tool for resolving and downloading" );
		System.out.println( "JVM dependencies from Maven repositories." );
		System.out.println();
		System.out.println( "Options:" );
		System.out.println( "  --help, -h              Show this help" );
		System.out.println( "  --properties=, -p       Use this properties file (default: creel.properties)" );
		System.out.println( "  --destination=, -d      Download to this directory (default: lib)" );
		System.out.println( "  --database=, -b         Artifact database file (default: [destination]/.creel)" );
		System.out.println( "  --platform=, -l         Set the default platform (default: maven)" );
		System.out.println( "  --quiet, -q             Quiet mode: don't output anything" );
		System.out.println( "  --ansi, -a              ANSI terminal output: pretty colors and animations" );
		System.out.println( "  --overwrite, -o         Overwrite files if they already exist" );
		System.out.println( "  --flat, -f              Flat file structure (no subdirectories)" );
		System.out.println( "  --multithreaded=, -m    Toggle multi-threaded mode (default: true)" );
		System.out.println();
		System.out.println( "For more information see: https://github.com/tliron/creel" );
	}
}
