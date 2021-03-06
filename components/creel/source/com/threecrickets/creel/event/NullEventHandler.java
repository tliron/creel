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

package com.threecrickets.creel.event;

/**
 * An event handler that does nothing.
 * 
 * @author Tal Liron
 */
public class NullEventHandler implements EventHandler
{
	//
	// Constants
	//

	/**
	 * The singleton.
	 */
	public static final NullEventHandler INSTANCE = new NullEventHandler();

	//
	// EventHandler
	//

	public boolean handleEvent( Event event )
	{
		return false;
	}
}
