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

/**
 * Thrown when a module identifier, module specification, or repository
 * reference a platform that is not supported by the engine.
 * 
 * @author Tal Liron
 */
public class UnsupportedPlatformException extends CreelException
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param platform
	 *        The unsupported platform
	 */
	public UnsupportedPlatformException( String platform )
	{
		super( "Unsupported platform: " + platform );
		this.platform = platform;
	}

	//
	// Attributes
	//

	/**
	 * The unsupported platform.
	 * 
	 * @return The unsupported platform
	 */
	public String getPlatform()
	{
		return platform;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String platform;
}
