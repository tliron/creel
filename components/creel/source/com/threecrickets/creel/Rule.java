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
import java.util.Map;

/**
 * @author Tal Liron
 */
public class Rule extends HashMap<String, String>
{
	//
	// Construction
	//

	public Rule( String platform, String type )
	{
		put( "platform", platform );
		put( "type", type );
	}

	public Rule( Map<String, ?> config, String defaultPlatform )
	{
		for( Map.Entry<String, ?> entry : config.entrySet() )
			put( entry.getKey(), entry.getValue().toString() );
		if( !containsKey( "platform" ) )
			put( "platform", defaultPlatform );
	}

	//
	// Attributes
	//

	public String getPlatform()
	{
		return get( "platform" );
	}

	public String getType()
	{
		return get( "type" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

}
