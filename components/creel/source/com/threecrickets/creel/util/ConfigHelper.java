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

package com.threecrickets.creel.util;

import java.util.Map;

/**
 * Help with parsing configs.
 * 
 * @author Tal Liron
 */
public class ConfigHelper
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param config
	 *        The config
	 */
	public ConfigHelper( Map<String, ?> config )
	{
		this.config = config;
	}

	//
	// Attributes
	//

	/**
	 * Gets a string value.
	 * 
	 * @param key
	 *        The key
	 * @return The value or null if not found
	 */
	public String getString( String key )
	{
		return getString( key, null );
	}

	/**
	 * Gets a string value.
	 * 
	 * @param key
	 *        The key
	 * @param defaultValue
	 *        The default value to return if not found
	 * @return The value
	 */
	public String getString( String key, String defaultValue )
	{
		Object value = config.get( key );
		return value != null ? value.toString() : defaultValue;
	}

	/**
	 * Gets an int value. Supports both {@link Number} and {@link String}
	 * instances.
	 * 
	 * @param key
	 *        The key
	 * @return The value or 0 if not found
	 */
	public int getInt( String key )
	{
		return getInt( key, 0 );
	}

	/**
	 * Gets an int value. Supports both {@link Number} and {@link String}
	 * instances.
	 * 
	 * @param key
	 *        The key
	 * @param defaultValue
	 *        The default value to return if not found
	 * @return The value
	 */
	public int getInt( String key, int defaultValue )
	{
		Object value = config.get( key );
		if( value instanceof Number )
			return ( (Number) value ).intValue();
		return value != null ? Integer.parseInt( value.toString() ) : defaultValue;
	}

	/**
	 * Gets a boolean value. Supports {@link Boolean}, {@link Number} (non-0 for
	 * true), and {@link String} instances.
	 * 
	 * @param key
	 *        The key
	 * @return The value or false if not found
	 */
	public boolean getBoolean( String key )
	{
		return getBoolean( key, false );
	}

	/**
	 * Gets a boolean value. Supports {@link Boolean}, {@link Number} (non-0 for
	 * true), and {@link String} instances.
	 * 
	 * @param key
	 *        The key
	 * @param defaultValue
	 *        The default value to return if not found
	 * @return The value
	 */
	public boolean getBoolean( String key, boolean defaultValue )
	{
		Object value = config.get( key );
		if( value instanceof Boolean )
			return (Boolean) value;
		if( value instanceof Number )
			return ( (Number) value ).intValue() != 0;
		return value != null ? Boolean.parseBoolean( value.toString() ) : defaultValue;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, ?> config;
}
