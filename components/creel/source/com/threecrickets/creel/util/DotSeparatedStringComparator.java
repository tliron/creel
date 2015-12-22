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

import java.util.Comparator;

/**
 * @author Tal Liron
 * @param <T>
 */
public class DotSeparatedStringComparator<T> implements Comparator<T>
{
	//
	// Comparator
	//

	@Override
	public int compare( T a1, T a2 )
	{
		String s1 = a1.toString();
		String s2 = a2.toString();
		String[] parts1 = s1.split( "\\." );
		String[] parts2 = s2.split( "\\." );

		int length1 = parts1.length;
		int length2 = parts2.length;
		int length = length1 > length2 ? length1 : length2;

		for( int p = 0; p < length; p++ )
		{
			String part1 = ( p < length1 ) ? parts1[p] : "";
			String part2 = ( p < length2 ) ? parts2[p] : "";

			if( !part1.isEmpty() && Character.isDigit( part1.charAt( 0 ) ) && !part2.isEmpty() && Character.isDigit( part2.charAt( 0 ) ) )
			{
				try
				{
					int part1int = Integer.parseInt( part1 );
					int part2int = Integer.parseInt( part2 );

					// Integer comparison
					if( part1int == part2int )
						continue;
					return part1int - part2int;
				}
				catch( NumberFormatException x )
				{
				}
			}

			// String comparison
			if( part1.equals( part2 ) )
				continue;
			return part1.compareTo( part2 );
		}

		return 0;
	}
}
