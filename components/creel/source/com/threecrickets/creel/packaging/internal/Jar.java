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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.threecrickets.creel.exception.CreelException;

/**
 * Utility class for working with Jars.
 * 
 * @author Tal Liron
 */
public class Jar
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param manifestUrl
	 *        The manifest URL
	 * @param rootDir
	 *        The root directory
	 * @param errorMessage
	 *        The error message prefix
	 */
	public Jar( URL manifestUrl, File rootDir, String errorMessage )
	{
		if( !"jar".equalsIgnoreCase( manifestUrl.getProtocol() ) )
			throw new CreelException( errorMessage + " is not in a jar file: " + manifestUrl );

		JarURLConnection connection;
		try
		{
			connection = (JarURLConnection) manifestUrl.openConnection();
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not read jar file: " + manifestUrl, x );
		}

		url = connection.getJarFileURL();
		try
		{
			file = rootDir.toPath().relativize( new File( url.toURI() ).toPath() ).toFile();
		}
		catch( URISyntaxException x )
		{
			throw new CreelException( "Parsing error in package: " + manifestUrl, x );
		}

		try
		{
			JarFile jarFile = connection.getJarFile();
			entries = new ArrayList<JarEntry>( jarFile.size() );
			for( Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); )
			{
				JarEntry entry = e.nextElement();
				if( !entry.isDirectory() )
					entries.add( entry );
			}
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not unpack jar file: " + file, x );
		}
	}

	//
	// Attributes
	//

	/**
	 * The Jar URL.
	 * 
	 * @return The Jar URL
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * The Jar file.
	 * 
	 * @return The Jar file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * The Jar file entries.
	 * 
	 * @return The Jar file entries
	 */
	public Iterable<JarEntry> getEntries()
	{
		return Collections.unmodifiableCollection( entries );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final File file;

	private final Collection<JarEntry> entries;
}