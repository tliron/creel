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
		String statePath = argumentsHelper.getString( "state", "s", null );
		int end = argumentsHelper.getInt( "end", "e", 4 );
		String defaultPlatform = argumentsHelper.getString( "platform", "l", "maven" );
		boolean quiet = argumentsHelper.hasSwitch( "quiet", "q" );
		boolean ansi = argumentsHelper.hasSwitch( "ansi", "a" );
		boolean overwrite = argumentsHelper.hasSwitch( "overwrite", "o" );
		boolean flat = argumentsHelper.hasSwitch( "flat", "f" );
		boolean multithreaded = argumentsHelper.getBoolean( "multithreaded", "m", true );

		Manager manager = new Manager();
		if( !quiet )
			( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( ansi ) );

		try
		{
			manager.setMultithreaded( multithreaded );
			manager.setDefaultPlatform( defaultPlatform );
			manager.setRootDir( destinationPath );
			manager.setStateFile( statePath );
			manager.setOverwrite( overwrite );
			manager.setFlat( flat );

			Properties properties = new Properties( new File( propertiesPath ) );
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
			manager.error( x );
		}
	}

	public static void help()
	{
		System.out.println( "Creel is a lightweight and lightning-fast tool for resolving and downloading" );
		System.out.println( "JVM dependencies from Maven repositories." );
		System.out.println();
		System.out.println( "Options:" );
		System.out.println( "  --help, -h              Show this help" );
		System.out.println( "  --properties=, -p       Use properties file (default: creel.properties)" );
		System.out.println( "  --destination=, -d      Download to directory (default: lib)" );
		System.out.println( "  --state=, -s            State file (default: [destination]/.creel)" );
		System.out.println( "  --end=, -e              Where to end: 1=identify, 2=install, 3=unpack, 4=delete redundant (default: 4)" );
		System.out.println( "  --platform=, -l         Set default platform (default: maven)" );
		System.out.println( "  --quiet, -q             Quiet mode: don't output anything" );
		System.out.println( "  --ansi, -a              ANSI terminal output: pretty colors and animations" );
		System.out.println( "  --overwrite, -o         Overwrite files if they already exist" );
		System.out.println( "  --flat, -f              Flat file structure (no subdirectories)" );
		System.out.println( "  --multithreaded=, -m    Set multi-threaded mode (default: true)" );
		System.out.println();
		System.out.println( "For more information see: https://github.com/tliron/creel" );
	}
}
