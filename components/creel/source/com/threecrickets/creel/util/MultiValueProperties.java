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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sorted JVM properties with support for a special three-part dot notation for
 * multiple value configs.
 * <p>
 * The first part is the prefix, the second part is a running integer of the
 * instance index, and the third part is the config key. For example:
 * 
 * <pre>
 * {@code
 * prefix.1.firstName=value1
 * prefix.1.lastName=value2
 * prefix.2.firstName=value3
 * prefix.2.lastName=value4
 * }
 * </pre>
 * 
 * Actually the index number does not have to be in sequence: it just has to be
 * unique, and is used for sorting.
 * 
 * @author Tal Liron
 */
public class MultiValueProperties extends SortedProperties
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public MultiValueProperties()
	{
		super( new DotSeparatedStringComparator<Object>() );
	}

	//
	// Operations
	//

	/**
	 * Puts a value using the three-part dot notation.
	 * 
	 * @param prefix
	 *        The prefix
	 * @param index
	 *        The instance index
	 * @param key
	 *        The key
	 * @param value
	 *        The value
	 * @return True if the key did not already exist
	 */
	public Object put( String prefix, int index, Object key, Object value )
	{
		return put( prefix + '.' + Integer.toString( index ) + '.' + key, value.toString() );
	}

	/**
	 * Puts an entire map's keys and values using the two-part dot notation.
	 * 
	 * @param prefix
	 *        The prefix
	 * @param index
	 *        The instance index
	 * @param map
	 *        The map
	 */
	public void putMap( String prefix, int index, Map<String, Object> map )
	{
		for( Map.Entry<String, Object> entry : map.entrySet() )
			put( prefix, index, entry.getKey(), entry.getValue() );
	}

	/**
	 * Converts three-part dot notation properties to maps.
	 * 
	 * @param prefix
	 *        The prefix
	 * @return The maps for the prefix
	 */
	public Iterable<Map<String, String>> toMaps( String prefix )
	{
		Map<Integer, Map<String, String>> configs = new HashMap<Integer, Map<String, String>>();
		for( Map.Entry<Object, Object> entry : entrySet() )
		{
			String key = entry.getKey().toString();
			String[] parts = key.split( "\\.", 3 );
			if( parts.length < 3 )
				continue;

			String thePrefix = parts[0];
			if( !prefix.equals( thePrefix ) )
				continue;

			int index;
			try
			{
				index = Integer.parseInt( parts[1] );
			}
			catch( NumberFormatException x )
			{
				continue;
			}

			String name = parts[2];

			Map<String, String> config = configs.get( index );
			if( config == null )
			{
				config = new HashMap<String, String>();
				configs.put( index, config );
			}

			config.put( name, entry.getValue().toString() );
		}
		return Collections.unmodifiableCollection( configs.values() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

}
