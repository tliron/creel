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

/**
 * Throws when the engine fails.
 * 
 * @author Tal Liron
 */
public class CreelException extends RuntimeException
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        The message
	 */
	public CreelException( String message )
	{
		super( message );
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *        The message
	 * @param cause
	 *        The cause
	 */
	public CreelException( String message, Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *        The cause
	 */
	public CreelException( Throwable cause )
	{
		super( cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
