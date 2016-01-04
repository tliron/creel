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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

/**
 * JVM properties sorted by their keys.
 * 
 * @author Tal Liron
 */
public class SortedProperties extends Properties
{
	//
	// Construction
	//

	/**
	 * Constructor using natural order.
	 */
	public SortedProperties()
	{
		this( null );
	}

	/**
	 * Constructor.
	 * 
	 * @param comparator
	 *        The comparator or null to use natural order.
	 */
	public SortedProperties( Comparator<Object> comparator )
	{
		super();
		this.comparator = comparator;
	}

	//
	// Properties
	//

	@Override
	public synchronized Enumeration<Object> keys()
	{
		Collection<Object> keys = new TreeSet<Object>( comparator );
		keys.addAll( keySet() );
		return Collections.enumeration( keys );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Comparator<Object> comparator;
}
