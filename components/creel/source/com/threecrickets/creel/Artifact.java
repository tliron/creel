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

import com.threecrickets.creel.util.DigestUtil;
import com.threecrickets.creel.util.HexUtil;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.IoUtil.ProgressListener;

/**
 * @author Tal Liron
 */
public class Artifact
{
	//
	// Construction
	//

	public Artifact( File file, URL sourceUrl, boolean isVolatile )
	{
		try
		{
			this.file = file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new RuntimeException( "Could not access artifact file: " + file, x );
		}
		this.sourceUrl = sourceUrl;
		this.isVolatile = isVolatile;
	}

	public Artifact( Map<String, String> config, File rootDir )
	{
		String url = config.get( "url" );
		if( url == null )
			throw new RuntimeException( "Missing URL" );
		try
		{
			this.sourceUrl = new URL( url );
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( "Bad URL: " + url );
		}
		String file = config.get( "file" );
		if( file == null )
			throw new RuntimeException( "Missing file" );
		this.file = rootDir != null ? new File( rootDir, file ) : new File( file );
		String isVolatile = config.get( "volatile" );
		this.isVolatile = isVolatile != null ? Boolean.valueOf( isVolatile ) : false;
		String digest = config.get( "digest" );
		if( digest != null )
			this.digest = HexUtil.fromHex( digest );
	}

	//
	// Attributes
	//

	public File getFile()
	{
		return file;
	}

	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	public boolean isVolatile()
	{
		return isVolatile;
	}

	public byte[] getDigest()
	{
		return digest;
	}

	//
	// Operations
	//

	public Map<String, Object> toConfig( File rootDir )
	{
		Map<String, Object> config = new HashMap<String, Object>();
		config.put( "url", getSourceUrl().toString() );
		Path path = getFile().toPath();
		try
		{
			config.put( "file", rootDir.toPath().relativize( path ) );
		}
		catch( IllegalArgumentException x )
		{
			config.put( "file", path );
		}
		if( isVolatile() )
			config.put( "volatile", true );
		if( getDigest() != null )
			config.put( "digest", HexUtil.toHex( getDigest() ) );
		return Collections.unmodifiableMap( config );
	}

	public boolean exists()
	{
		return getFile().exists();
	}

	public boolean wasModified() throws IOException
	{
		if( ( getDigest() == null ) || !exists() )
			return true;
		byte[] currentDigest = DigestUtil.getDigest( getFile(), ALGORITHM );
		return !Arrays.equals( getDigest(), currentDigest );
	}

	public boolean isDifferent() throws IOException
	{
		byte[] currentDigest = DigestUtil.getDigest( getFile(), ALGORITHM );
		byte[] sourceDigest = DigestUtil.getDigest( getSourceUrl(), ALGORITHM );
		return !Arrays.equals( currentDigest, sourceDigest );
	}

	public void copy( ProgressListener progressListener ) throws IOException
	{
		IoUtil.copy( getSourceUrl(), getFile(), progressListener );
	}

	public boolean delete( File rootDir )
	{
		return IoUtil.deleteWithParentDirectories( getFile(), rootDir );
	}

	public void updateDigest() throws IOException
	{
		digest = DigestUtil.getDigest( getFile(), ALGORITHM );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return "file: " + getFile() + ", sourceUrl: " + getSourceUrl();
	}

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		Artifact artifact = (Artifact) object;
		return getFile().equals( artifact.getFile() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getFile() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ALGORITHM = "SHA-1";

	private final File file;

	private final URL sourceUrl;

	private final boolean isVolatile;

	private byte[] digest;
}
