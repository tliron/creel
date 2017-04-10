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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.threecrickets.creel.util.IoUtil;

/**
 * Creel configuration, formatted as a JVM properties file.
 * 
 * @author Tal Liron
 */
public class Configuration extends java.util.Properties
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param file
	 *        The file
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public Configuration( File file ) throws IOException
	{
		this( new BufferedReader( new FileReader( file ), IoUtil.bufferSize ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param reader
	 *        The reader (will be closed)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public Configuration( Reader reader ) throws IOException
	{
		try
		{
			load( reader );
		}
		finally
		{
			reader.close();
		}
	}

	//
	// Attributes
	//

	/**
	 * Gets a boolean value.
	 * 
	 * @param key
	 *        The key
	 * @param defaultValue
	 *        The default value to return if key not found
	 * @return The value
	 */
	public Boolean getBoolean( String key, Boolean defaultValue )
	{
		String value = getProperty( key );
		return value != null ? new Boolean( value ) : defaultValue;
	}

	/**
	 * Gets an integer value.
	 * 
	 * @param key
	 *        The key
	 * @param defaultValue
	 *        The default value to return if key not found
	 * @return The value
	 */
	public Integer getInteger( String key, Integer defaultValue )
	{
		String value = getProperty( key );
		return value != null ? new Integer( value ) : defaultValue;
	}

	/**
	 * Gets all the module specification configs.
	 * 
	 * @return The module specification configs
	 */
	public Collection<Map<String, ?>> getModuleSpecificationConfigs()
	{
		return getConfigs( "module" );
	}

	/**
	 * Gets all the repository configs.
	 * 
	 * @return The repository configs
	 */
	public Collection<Map<String, ?>> getRepositoryConfigs()
	{
		return getConfigs( "repository" );
	}

	/**
	 * Gets all the rule configs.
	 * 
	 * @return The rule configs
	 */
	public Collection<Map<String, ?>> getRuleConfigs()
	{
		return getConfigs( "rule" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private Collection<Map<String, ?>> getConfigs( String type )
	{
		SortedMap<Integer, Map<String, ?>> configs = new TreeMap<Integer, Map<String, ?>>();

		for( Map.Entry<Object, Object> entry : entrySet() )
		{
			String name = entry.getKey().toString();
			if( name.startsWith( type + '.' ) )
			{
				String[] parts = name.split( "\\." );

				if( parts.length != 3 )
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
				String attribute = parts[2];

				@SuppressWarnings("unchecked")
				Map<String, Object> config = (Map<String, Object>) configs.get( index );
				if( config == null )
				{
					config = new HashMap<String, Object>();
					configs.put( index, config );
				}

				config.put( attribute, entry.getValue() );
			}
		}

		return Collections.unmodifiableCollection( configs.values() );
	}
}
