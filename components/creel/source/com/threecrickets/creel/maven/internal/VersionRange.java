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

package com.threecrickets.creel.maven.internal;

/**
 * Parsed Maven version range.
 * 
 * @author Tal Liron
 */
public class VersionRange
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param start
	 *        Start version
	 * @param end
	 *        End version
	 * @param includeStart
	 *        Whether to include the start version in the range
	 * @param includeEnd
	 *        Whether to include the end version in the range
	 */
	public VersionRange( String start, String end, boolean includeStart, boolean includeEnd )
	{
		this.start = new Version( start );
		this.end = new Version( end );
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	/**
	 * Constructor.
	 * 
	 * @param start
	 *        Start version
	 * @param end
	 *        End version
	 * @param includeStart
	 *        Whether to include the start version in the range
	 * @param includeEnd
	 *        Whether to include the end version in the range
	 */
	public VersionRange( Version start, Version end, boolean includeStart, boolean includeEnd )
	{
		this.start = start;
		this.end = end;
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	//
	// Attributes
	//

	/**
	 * The start version.
	 * 
	 * @return The start version
	 */
	public Version getStart()
	{
		return start;
	}

	/**
	 * The end version.
	 * 
	 * @return The end version
	 */
	public Version getEnd()
	{
		return end;
	}

	/**
	 * Whether to include the start version in the range.
	 * 
	 * @return True if included
	 */
	public boolean isIncludeStart()
	{
		return includeStart;
	}

	/**
	 * Whether to include the end version in the range.
	 * 
	 * @return True if included
	 */
	public boolean isIncludeEnd()
	{
		return includeEnd;
	}

	//
	// Operations
	//

	/**
	 * Checks whether the version is in the range.
	 * 
	 * @param version
	 *        The version
	 * @return True if in range
	 */
	public boolean in( Version version )
	{
		int compareStart = !getStart().isNull() ? version.compareTo( getStart() ) : 1;
		int compareEnd = !getEnd().isNull() ? getEnd().compareTo( version ) : 1;

		if( isIncludeStart() && isIncludeEnd() )
			return ( compareStart >= 0 ) && ( compareEnd >= 0 );
		else if( isIncludeStart() && !isIncludeEnd() )
			return ( compareStart >= 0 ) && ( compareEnd > 0 );
		else if( !isIncludeStart() && isIncludeEnd() )
			return ( compareStart > 0 ) && ( compareEnd >= 0 );
		else
			return ( compareStart > 0 ) && ( compareEnd > 0 );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return ( isIncludeStart() ? "[" : "(" ) + ( getStart() != null ? getStart() : "" ) + ',' + ( getEnd() != null ? getEnd() : "" ) + ( isIncludeEnd() ? ']' : ')' );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Version start;

	private final Version end;

	private final boolean includeStart;

	private final boolean includeEnd;
}
