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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.util.ClassUtil;
import com.threecrickets.creel.util.DigestUtil;
import com.threecrickets.creel.util.HexUtil;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.ProgressListener;

/**
 * Represents a named file copied/downloaded from a source URL.
 * <p>
 * An artifact can be marked as "volatile," meaning that it is expected to be
 * modified after the initial copy/download. A cryptographic digest (SHA-1 by
 * default) is used to track changes to the artifact.
 * 
 * @author Tal Liron
 */
public class Artifact implements Comparable<Artifact>
{
	//
	// Constants
	//

	/**
	 * Artifact type.
	 */
	public enum Type
	{
		/**
		 * Contains executable code and related resources (".class" files,
		 * etc.).
		 */
		LIBRARY,
		/**
		 * Contains API reference material (JavaDocs, manuals, etc.).
		 */
		API,
		/**
		 * Contains source code (".java" files, etc.).
		 */
		SOURCE;

		public static Type valueOfNonStrict( String value )
		{
			return ClassUtil.valueOfNonStrict( Type.class, value );
		}
	};

	//
	// Static attributes
	//

	/**
	 * The algorithm for digests. Defaults to SHA-1.
	 */
	public static volatile String algorithm = "SHA-1";

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param file
	 *        The file
	 * @param sourceUrl
	 *        The source URL
	 * @param isVolatile
	 *        Whether the artifact is volatile
	 */
	public Artifact( File file, URL sourceUrl, boolean isVolatile )
	{
		this( null, null, file, sourceUrl, isVolatile );
	}

	/**
	 * Constructor.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param type
	 *        The type
	 * @param file
	 *        The file
	 * @param sourceUrl
	 *        The source URL
	 * @param isVolatile
	 *        Whether the artifact is volatile
	 */
	public Artifact( ModuleIdentifier moduleIdentifier, Type type, File file, URL sourceUrl, boolean isVolatile )
	{
		try
		{
			this.file = file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not access artifact file: " + file, x );
		}
		this.moduleIdentifier = moduleIdentifier != null ? moduleIdentifier.toString() : null;
		this.type = type;
		this.sourceUrl = sourceUrl;
		this.isVolatile = isVolatile;
	}

	/**
	 * Config constructor.
	 * 
	 * @param config
	 *        The config
	 * @param directories
	 *        The directories in which to install artifacts
	 * @see Artifact#toConfig(Directories)
	 */
	public Artifact( Map<String, String> config, Directories directories )
	{
		moduleIdentifier = config.get( "module" );
		String type = config.get( "type" );
		this.type = Type.valueOfNonStrict( type );
		File rootDir = directories.getFor( this );
		if( rootDir == null )
			throw new CreelException( "Unsupported type: " + type );
		String file = config.get( "file" );
		if( file == null )
			throw new CreelException( "Missing file" );
		this.file = new File( rootDir, file );
		String url = config.get( "url" );
		if( url == null )
			throw new CreelException( "Missing URL" );
		try
		{
			this.sourceUrl = new URL( url );
		}
		catch( MalformedURLException x )
		{
			throw new CreelException( "Bad URL: " + url );
		}
		String isVolatile = config.get( "volatile" );
		this.isVolatile = isVolatile != null ? Boolean.valueOf( isVolatile ) : false;
		String digest = config.get( "digest" );
		if( digest != null )
			this.digest = HexUtil.fromHex( digest );
	}

	//
	// Attributes
	//

	/**
	 * The module identifier.
	 * 
	 * @return The module identifier
	 */
	public String getModuleIdentifier()
	{
		return moduleIdentifier;
	}

	/**
	 * The type.
	 * 
	 * @return The type
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * The file.
	 * 
	 * @return The file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * The source URL.
	 * 
	 * @return The source URL
	 */
	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	/**
	 * Whether the artifact is volatile.
	 * 
	 * @return True if volatile
	 */
	public boolean isVolatile()
	{
		return isVolatile;
	}

	/**
	 * The digest.
	 * 
	 * @return The digest or null
	 */
	public byte[] getDigest()
	{
		return digest;
	}

	//
	// Operations
	//

	/**
	 * Converts the artifact to a config.
	 * 
	 * @param directories
	 *        The directories in which to install artifacts
	 * @return The config
	 * @see Artifact#Artifact(Map, Directories)
	 */
	public Map<String, Object> toConfig( Directories directories )
	{
		Map<String, Object> config = new HashMap<String, Object>();
		if( getModuleIdentifier() != null )
			config.put( "module", getModuleIdentifier() );
		if( getType() != null )
			config.put( "type", getType().toString() );
		config.put( "url", getSourceUrl().toString() );
		Path path = getFile().toPath();
		File rootDir = directories.getFor( this );
		if( rootDir != null )
		{
			try
			{
				config.put( "file", rootDir.toPath().relativize( path ) );
			}
			catch( IllegalArgumentException x )
			{
				config.put( "file", path );
			}
		}
		if( isVolatile() )
			config.put( "volatile", true );
		if( getDigest() != null )
			config.put( "digest", HexUtil.toHex( getDigest() ) );
		return Collections.unmodifiableMap( config );
	}

	/**
	 * Checks whether the file exists.
	 * 
	 * @return True if exists
	 */
	public boolean exists()
	{
		return getFile().exists();
	}

	/**
	 * Checks whether the file was modified by comparing its current digest to
	 * the stored digest.
	 * <p>
	 * If there is no digest stored or the file does not exist will return true.
	 * 
	 * @return True if was modified
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public boolean wasModified() throws IOException
	{
		if( ( getDigest() == null ) || !exists() )
			return true;
		byte[] currentDigest = DigestUtil.getDigest( getFile(), algorithm );
		return !Arrays.equals( getDigest(), currentDigest );
	}

	/**
	 * Checks whether the file is different from the content of the source URL
	 * by comparing their current digests.
	 * 
	 * @return True if different
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public boolean isDifferent() throws IOException
	{
		byte[] currentDigest = DigestUtil.getDigest( getFile(), algorithm );
		byte[] sourceDigest = DigestUtil.getDigest( getSourceUrl(), algorithm );
		return !Arrays.equals( currentDigest, sourceDigest );
	}

	/**
	 * Copies the file's content from the source URL, overwriting it if it
	 * already exists.
	 * 
	 * @param progressListener
	 *        The progress listener or null
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public void copy( ProgressListener progressListener ) throws IOException
	{
		IoUtil.copy( getSourceUrl(), getFile(), progressListener );
	}

	/**
	 * Deletes the file, including empty parent directories up to the root
	 * directory.
	 * 
	 * @param directories
	 *        The directories in which to install artifacts
	 * @return True if deleted
	 */
	public boolean delete( Directories directories )
	{
		File rootDir = directories.getFor( this );
		if( rootDir != null )
			return IoUtil.deleteWithParentDirectories( getFile(), rootDir );
		else
			return getFile().delete();
	}

	/**
	 * Updates the stored digest to the current value.
	 * 
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public void updateDigest() throws IOException
	{
		digest = DigestUtil.getDigest( getFile(), algorithm );
	}

	//
	// Comparable
	//

	public int compareTo( Artifact artifact )
	{
		int c;
		Type t1 = getType();
		Type t2 = artifact.getType();
		if( ( t1 == null ) && ( t2 == null ) )
			c = 0;
		else if( t1 == null )
			c = 1;
		else if( t2 == null )
			c = -1;
		else
			c = t1.compareTo( t2 );
		return c == 0 ? getFile().compareTo( artifact.getFile() ) : c;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return "file: " + getFile() + ", sourceUrl: " + getSourceUrl();
	}

	/*
	 * Note that we are <b>only</b> using the file for the equality check.
	 */
	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		Artifact artifact = (Artifact) object;
		return getFile().equals( artifact.getFile() );
	}

	/*
	 * Note that we are <b>only</b> using the file for the hash.
	 */
	@Override
	public int hashCode()
	{
		return Objects.hash( getFile() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String moduleIdentifier;

	private final Type type;

	private final File file;

	private final URL sourceUrl;

	private final boolean isVolatile;

	private byte[] digest;
}
