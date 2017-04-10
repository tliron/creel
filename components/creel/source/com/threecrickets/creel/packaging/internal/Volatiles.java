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

package com.threecrickets.creel.packaging.internal;

import java.util.jar.Attributes;

import com.threecrickets.creel.packaging.PackagingUtil;

/**
 * Manages volatile entries within a package.
 * 
 * @author Tal Liron
 */
public class Volatiles
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param manifest
	 *        The manifest attributes
	 */
	public Volatiles( Attributes manifest )
	{
		// Volatile folders
		Object packageVolatileFolders = manifest.getValue( PackagingUtil.PACKAGE_VOLATILE_FOLDERS );
		folders = packageVolatileFolders != null ? packageVolatileFolders.toString().split( "," ) : null;

		// Make sure we have a trailing slash
		if( folders != null )
			for( int i = folders.length - 1; i >= 0; i-- )
				if( !folders[i].endsWith( "/" ) )
					folders[i] += "/";

		// Volatile files
		Object packageVolatileFiles = manifest.getValue( PackagingUtil.PACKAGE_VOLATILE_FILES );
		files = packageVolatileFiles != null ? packageVolatileFiles.toString().split( "," ) : null;
	}

	//
	// Operations
	//

	/**
	 * Checks if the filename is marked volatile.
	 * 
	 * @param filename
	 *        The filename
	 * @return True if volatile
	 */
	public boolean contains( String filename )
	{
		if( folders != null )
			for( String folder : folders )
				if( filename.startsWith( folder ) )
					return true;
		if( files != null )
			for( String file : files )
				if( filename.equals( file ) )
					return true;
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String[] folders;

	private final String[] files;
}