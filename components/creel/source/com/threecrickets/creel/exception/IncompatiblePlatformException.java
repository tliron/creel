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
 * Thrown when a module identifier, module specification, or repository are used
 * with others of a different platforms.
 * 
 * @author Tal Liron
 */
public class IncompatiblePlatformException extends ClassCastException
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param platform
	 *        The platform
	 * @param object
	 *        The incompatible object
	 */
	public IncompatiblePlatformException( String platform, Object object )
	{
		super( "Object incompatible with platform: " + platform );
		this.platform = platform;
		this.object = object;
	}

	//
	// Attributes
	//

	/**
	 * The platform.
	 * 
	 * @return The platform
	 */
	public String getPlatform()
	{
		return platform;
	}

	/**
	 * The incompatible object.
	 * 
	 * @return The incompatible object
	 */
	public Object getObject()
	{
		return object;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String platform;

	private final Object object;
}
