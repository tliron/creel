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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tal Liron
 */
public class MultiValueProperties extends SortedProperties
{
	//
	// Construction
	//

	public MultiValueProperties()
	{
		super( new DotSeparatedStringComparator<Object>() );
	}

	//
	// Operations
	//

	public Object put( int index, Object key, Object value )
	{
		return put( Integer.toString( index ) + '.' + key, value );
	}

	public Iterable<Map<String, String>> toMaps()
	{
		Map<Integer, Map<String, String>> configs = new HashMap<Integer, Map<String, String>>();
		for( Map.Entry<Object, Object> entry : entrySet() )
		{
			String key = entry.getKey().toString();
			String[] parts = key.split( "\\." );
			if( parts.length != 2 )
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

			String name = parts[1];

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
