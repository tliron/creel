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

import java.util.HashMap;

/**
 * @author Tal Liron
 */
public class Command extends HashMap<String, Object>
{
	//
	// Construction
	//

	public Command( String type )
	{
		put( "type", type );
	}

	//
	// Attributes
	//

	public String getType()
	{
		return get( "type" ).toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

}
