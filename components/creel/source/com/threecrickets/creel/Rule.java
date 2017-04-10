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
import java.util.Map;

/**
 * Rules are used to manipulate the module dependency tree, such as excluding
 * modules, rewriting them, etc.
 * <p>
 * They are implemented differently per platform, via
 * {@link Repository#applyRule(Module, Rule, com.threecrickets.creel.event.Notifier)}
 * .
 * 
 * @author Tal Liron
 */
public class Rule extends HashMap<String, String>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param platform
	 *        The platform
	 * @param type
	 *        The rule type
	 */
	public Rule( String platform, String type )
	{
		put( "platform", platform );
		put( "type", type );
	}

	/**
	 * Config constructor.
	 * 
	 * @param config
	 *        The config
	 * @param defaultPlatform
	 *        The default platform to use if none is specified
	 */
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

	/**
	 * The platform.
	 * 
	 * @return The platform name
	 */
	public String getPlatform()
	{
		return get( "platform" );
	}

	/**
	 * The rule type.
	 * 
	 * @return The type
	 */
	public String getType()
	{
		return get( "type" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
