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

package com.threecrickets.creel.packaging.internal;

import java.util.jar.Attributes;

import com.threecrickets.creel.packaging.Packaging;

/**
 * Manages volatile entries within a package.
 * 
 * @author Tal Liron
 */
public class Volatiles
{
	public Volatiles( Attributes manifest )
	{
		// Volatile folders
		Object packageVolatileFolders = manifest.getValue( Packaging.PACKAGE_VOLATILE_FOLDERS );
		folders = packageVolatileFolders != null ? packageVolatileFolders.toString().split( "," ) : null;

		// Make sure we have a trailing slash
		if( folders != null )
			for( int i = folders.length - 1; i >= 0; i-- )
				if( !folders[i].endsWith( "/" ) )
					folders[i] += "/";

		// Volatile files
		Object packageVolatileFiles = manifest.getValue( Packaging.PACKAGE_VOLATILE_FILES );
		files = packageVolatileFiles != null ? packageVolatileFiles.toString().split( "," ) : null;
	}

	//
	// Operations
	//

	public boolean contains( String name )
	{
		if( folders != null )
			for( String folder : folders )
				if( name.startsWith( folder ) )
					return true;
		if( files != null )
			for( String file : files )
				if( name.equals( file ) )
					return true;
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String[] folders;

	private final String[] files;
}