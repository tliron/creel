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

package com.threecrickets.creel;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Directories in which to install artifacts.
 * 
 * @author Tal Liron
 */
public class Directories
{
	//
	// Attributes
	//

	public Map<Artifact.Type, File> getAll()
	{
		return Collections.unmodifiableMap( directories );
	}

	/**
	 * The directory according to the artifact's type.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return The directory or null if not set
	 */
	public File getFor( Artifact artifact )
	{
		return get( artifact.getType() );
	}

	/**
	 * The directory according to the artifact type.
	 * 
	 * @param type
	 *        The artifact type
	 * @return The directory or null if not set
	 */
	public File get( Artifact.Type type )
	{
		return directories.get( type );
	}

	/**
	 * The directory according to the artifact type.
	 * 
	 * @param type
	 *        The type
	 * @param directory
	 *        The directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void set( Artifact.Type type, String directory ) throws IOException
	{
		set( type, directory != null ? new File( directory ) : null );
	}

	/**
	 * The directory according to the artifact type.
	 * 
	 * @param type
	 *        The type
	 * @param directory
	 *        The directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void set( Artifact.Type type, File directory ) throws IOException
	{
		if( directory != null )
			directories.put( type, directory.getCanonicalFile() );
		else
			directories.remove( type );
	}

	/**
	 * Sets all the directories.
	 * 
	 * @param directories
	 *        The directories
	 */
	public void set( Map<Artifact.Type, File> directories )
	{
		this.directories.putAll( directories );
	}

	/**
	 * Sets all the directories.
	 * 
	 * @param directories
	 *        The directories
	 */
	public void set( Directories directories )
	{
		set( directories.getAll() );
	}

	/**
	 * The default directory in which to install artifacts. When null, will not
	 * install them.
	 * 
	 * @return The default directory or null
	 */
	public File getDefault()
	{
		return get( null );
	}

	/**
	 * The default directory in which to install artifacts. When null, will not
	 * install them.
	 * 
	 * @param directory
	 *        The default directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setDefault( String directory ) throws IOException
	{
		set( null, directory );
	}

	/**
	 * The default directory in which to install artifacts. When null, will not
	 * install them.
	 * 
	 * @param directory
	 *        The default directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setDefault( File directory ) throws IOException
	{
		set( null, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @return The library directory or null
	 */
	public File getLibrary()
	{
		return get( Artifact.Type.LIBRARY );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @param directory
	 *        The library directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setLibrary( String directory ) throws IOException
	{
		set( Artifact.Type.LIBRARY, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @param directory
	 *        The library directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setLibrary( File directory ) throws IOException
	{
		set( Artifact.Type.LIBRARY, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#API} artifacts.
	 * When null, will not install them.
	 * 
	 * @return The API directory or null
	 */
	public File getApi()
	{
		return get( Artifact.Type.API );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#API} artifacts.
	 * When null, will not install them.
	 * 
	 * @param directory
	 *        The API directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setApi( String directory ) throws IOException
	{
		set( Artifact.Type.API, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#API} artifacts.
	 * 
	 * @param directory
	 *        The API directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setApi( File directory ) throws IOException
	{
		set( Artifact.Type.API, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#SOURCE} artifacts.
	 * When null, will not install them.
	 * 
	 * @return The source directory or null
	 */
	public File getSource()
	{
		return get( Artifact.Type.SOURCE );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#SOURCE} artifacts.
	 * When null, will not install them.
	 * 
	 * @param directory
	 *        The source directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setSource( String directory ) throws IOException
	{
		set( Artifact.Type.SOURCE, directory );
	}

	/**
	 * The directory in which to install {@link Artifact.Type#SOURCE} artifacts.
	 * When null, will not install them.
	 * 
	 * @param directory
	 *        The source directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setSource( File directory ) throws IOException
	{
		set( Artifact.Type.SOURCE, directory );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<Artifact.Type, File> directories = new HashMap<Artifact.Type, File>();
}
