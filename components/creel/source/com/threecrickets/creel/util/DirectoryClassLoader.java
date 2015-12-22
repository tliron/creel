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

package com.threecrickets.creel.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Tal Liron
 */
public class DirectoryClassLoader extends URLClassLoader
{
	//
	// Construction
	//

	public DirectoryClassLoader( File rootDir )
	{
		this( rootDir, DirectoryClassLoader.class.getClassLoader() );
	}

	public DirectoryClassLoader( File rootDir, ClassLoader parent )
	{
		super( gatherJars( rootDir ), parent );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static URL[] gatherJars( File rootDir )
	{
		Collection<URL> urls = new ArrayList<URL>();
		gatherJars( rootDir, urls );
		return urls.toArray( new URL[urls.size()] );
	}

	private static void gatherJars( File file, Collection<URL> urls )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				gatherJars( child, urls );
		else if( file.getName().toLowerCase().endsWith( ".jar" ) )
		{
			try
			{
				urls.add( file.toURI().toURL() );
			}
			catch( MalformedURLException x )
			{
			}
		}
	}
}
