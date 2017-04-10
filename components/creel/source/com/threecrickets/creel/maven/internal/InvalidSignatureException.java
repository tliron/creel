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

package com.threecrickets.creel.maven.internal;

import com.threecrickets.creel.exception.CreelException;

/**
 * Thrown when downloaded files from a Maven repository don't match their
 * signature.
 * 
 * @author Tal Liron
 */
public class InvalidSignatureException extends CreelException
{
	//
	// Construction
	//

	public InvalidSignatureException()
	{
		super( "Invalid signature for Maven file" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
