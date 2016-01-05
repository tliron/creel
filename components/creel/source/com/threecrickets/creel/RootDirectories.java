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

/**
 * Root directories in which to install artifacts.
 * 
 * @author Tal Liron
 */
public class RootDirectories
{
	//
	// Attributes
	//

	/**
	 * The root directory according to the artifact's type.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return The root directory or null
	 */
	public File getRootFor( Artifact artifact )
	{
		Artifact.Type type = artifact.getType();
		if( type == Artifact.Type.LIBRARY )
			return getLibrary();
		else if( type == Artifact.Type.REFERENCE )
			return getReference();
		else if( type == Artifact.Type.SOURCE )
			return getSource();
		else
			return getOther();
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @return The library root directory or null
	 */
	public File getLibrary()
	{
		return library;
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @param libraryPath
	 *        The library root directory path or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setLibrary( String libraryPath ) throws IOException
	{
		setLibrary( libraryPath != null ? new File( libraryPath ) : null );
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#LIBRARY}
	 * artifacts. When null, will not install them.
	 * 
	 * @param library
	 *        The library root directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setLibrary( File library ) throws IOException
	{
		this.library = library != null ? library.getCanonicalFile() : null;
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#REFERENCE}
	 * artifacts. When null, will not install them.
	 * 
	 * @return The reference root directory or null
	 */
	public File getReference()
	{
		return reference;
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#REFERENCE}
	 * artifacts. When null, will not install them.
	 * 
	 * @param referencePath
	 *        The reference root directory path or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setReference( String referencePath ) throws IOException
	{
		setReference( referencePath != null ? new File( referencePath ) : null );
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#REFERENCE}
	 * artifacts.
	 * 
	 * @param reference
	 *        The reference root directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setReference( File reference ) throws IOException
	{
		this.reference = reference != null ? reference.getCanonicalFile() : null;
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#SOURCE}
	 * artifacts. When null, will not install them.
	 * 
	 * @return The source root directory or null
	 */
	public File getSource()
	{
		return source;
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#SOURCE}
	 * artifacts. When null, will not install them.
	 * 
	 * @param sourcePath
	 *        The source root directory path or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setSource( String sourcePath ) throws IOException
	{
		setSource( sourcePath != null ? new File( sourcePath ) : null );
	}

	/**
	 * The root directory in which to install {@link Artifact.Type#SOURCE}
	 * artifacts. When null, will not install them.
	 * 
	 * @param source
	 *        The source root directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setSource( File source ) throws IOException
	{
		this.source = source != null ? source.getCanonicalFile() : null;
	}

	/**
	 * The root directory in which to install artifacts of unknown type. When
	 * null, will not install them.
	 * 
	 * @return The other root directory or null
	 */
	public File getOther()
	{
		return other;
	}

	/**
	 * The root directory in which to install artifacts of unknown type. When
	 * null, will not install them.
	 * 
	 * @param otherPath
	 *        The other root directory path or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setOther( String otherPath ) throws IOException
	{
		setOther( otherPath != null ? new File( otherPath ) : null );
	}

	/**
	 * The root directory in which to install artifacts of unknown type. When
	 * null, will not install them.
	 * 
	 * @param other
	 *        The other root directory or null
	 * @throws IOException
	 *         In case the directory could not be accessed
	 */
	public void setOther( File other ) throws IOException
	{
		this.other = other != null ? other.getCanonicalFile() : null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private File library;

	private File reference;

	private File source;

	private File other;
}
