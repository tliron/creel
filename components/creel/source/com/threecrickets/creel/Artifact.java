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
import java.net.URL;
import java.util.Objects;

/**
 * @author Tal Liron
 */
public class Artifact
{
	//
	// Construction
	//

	public Artifact( File file, URL sourceUrl )
	{
		this.file = file;
		this.sourceUrl = sourceUrl;
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

	//
	// Operations
	//

	public boolean delete( File root )
	{
		File file = getFile();
		while( true )
		{
			if( file.isDirectory() )
			{
				if( !file.delete() )
					break;
			}
			else if( file.exists() && !file.delete() )
				return false;
			file = file.getParentFile();
			if( ( file == null ) || file.equals( root ) )
				break;
		}
		return true;
	}

	//
	// Objects
	//

	@Override
	public String toString()
	{
		return "file:" + getFile() + ", sourceUrl: " + getSourceUrl();
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

	private final File file;

	private final URL sourceUrl;
}
