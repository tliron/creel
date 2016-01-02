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

package com.threecrickets.creel.exception;

import java.io.File;

/**
 * @author Tal Liron
 */
public class InvalidSignatureException extends RuntimeException
{
	public InvalidSignatureException( File file )
	{
		super( "Invalid signature for file: " + file );
	}

	private static final long serialVersionUID = 1L;
}
