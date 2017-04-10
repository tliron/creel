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

package com.threecrickets.creel;

import java.util.HashMap;

import com.threecrickets.creel.event.Notifier;

/**
 * Commands are optionally returned by
 * {@link Repository#applyRule(Module, Rule, Notifier)} to be processed by the
 * engine.
 * 
 * @author Tal Liron
 */
public class Command extends HashMap<String, Object>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param type
	 *        The command type
	 */
	public Command( String type )
	{
		put( "type", type );
	}

	//
	// Attributes
	//

	/**
	 * The command type.
	 * 
	 * @return The command type
	 */
	public String getType()
	{
		return get( "type" ).toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

}
