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

package com.threecrickets.creel.exception;

import com.threecrickets.creel.Artifact;

/**
 * Thrown when an artifact fails validation.
 * 
 * @author Tal Liron
 */
public class InvalidArtifactException extends CreelException
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param artifact
	 *        The invalid artifact
	 */
	public InvalidArtifactException( Artifact artifact )
	{
		super( "Invalid artifact: " + artifact );
		this.artifact = artifact;
	}

	//
	// Attributes
	//

	/**
	 * The invalid artifact.
	 * 
	 * @return The invalid artifact
	 */
	public Artifact getArtifact()
	{
		return artifact;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Artifact artifact;
}
