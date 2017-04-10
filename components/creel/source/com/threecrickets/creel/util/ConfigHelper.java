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

package com.threecrickets.creel.util;

import java.util.Collection;
import java.util.HashMap;
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

	/**
	 * Gets a config containing entries whose keys begin with a prefix. The
	 * config's keys will be stripped of the prefix.
	 * 
	 * @param keyPrefix
	 *        The key prefix
	 * @return The map
	 */
	public Map<String, Object> getSubConfig( String keyPrefix )
	{
		int keyPrefixLength = keyPrefix.length();
		Map<String, Object> map = new HashMap<String, Object>();
		for( Map.Entry<String, ?> entry : config.entrySet() )
		{
			String key = entry.getKey();
			if( key.startsWith( keyPrefix ) )
			{
				key = key.substring( keyPrefixLength );
				map.put( key, entry.getValue() );
			}
		}
		return map;
	}

	/**
	 * Gets all configs containing entries whose keys begin with a prefix and
	 * then have a three-part dot notation. The configs' keys will be stripped
	 * of the prefix and the first two parts.
	 * 
	 * @param keyPrefix
	 *        The key prefix
	 * @return The map
	 */
	public Collection<Map<String, Object>> getSubConfigs( String keyPrefix )
	{
		Map<Integer, Map<String, Object>> maps = new HashMap<Integer, Map<String, Object>>();
		int keyPrefixLength = keyPrefix.length();
		for( Map.Entry<String, ?> entry : config.entrySet() )
		{
			String key = entry.getKey();
			if( key.startsWith( keyPrefix ) )
			{
				key = key.substring( keyPrefixLength );
				String[] parts = key.split( "\\.", 2 );
				if( parts.length < 2 )
					continue;

				int index;
				try
				{
					index = Integer.parseInt( parts[0] );
				}
				catch( NumberFormatException x )
				{
					continue;
				}

				key = parts[1];

				Map<String, Object> map = maps.get( index );
				if( map == null )
				{
					map = new HashMap<String, Object>();
					maps.put( index, map );
				}

				map.put( key, entry.getValue() );
			}
		}
		return maps.values();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, ?> config;
}
