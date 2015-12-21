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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tal Liron
 */
public class VersionSpecification
{
	//
	// Construction
	//

	public VersionSpecification( String versionSpecification, boolean strict )
	{
		this.strict = strict;
		versionSpecification = versionSpecification == null ? "" : versionSpecification.trim();
		text = versionSpecification;

		// Check if all
		if( isAll( versionSpecification ) )
		{
			all = true;
			trivial = false;
			ranges = null;
			return;
		}

		// Check if trivial
		if( isTrivial( versionSpecification ) )
		{
			all = false;
			trivial = true;
			ranges = null;
			return;
		}

		// TODO: regexp match to see if it's parseable

		all = false;
		trivial = false;
		ranges = new ArrayList<VersionRange>();

		// Convert Ivy range suffix to Maven: "1.0+" to "[1.0,)"
		if( !strict && versionSpecification.endsWith( "+" ) )
			versionSpecification = "[" + versionSpecification.substring( 0, versionSpecification.length() - 1 ) + ",)";

		Matcher matcher = PATTERN.matcher( versionSpecification );
		while( matcher.find() )
		{
			String start = matcher.group( 1 );
			String end = matcher.group( 2 );
			char open = versionSpecification.charAt( matcher.start() );
			char close = versionSpecification.charAt( matcher.end() - 1 );

			ranges.add( new VersionRange( start, end, open == '[', close == ']' ) );

			/*
			 * if (!matcher.find()) { // Make sure there is a comma in between
			 * ranges var between = version.substring(lastIndex, matches.index);
			 * if (!/^\s+,\s+$/.test(between)) return null; }
			 */
		}
	}

	//
	// Attributes
	//

	public boolean isStrict()
	{
		return strict;
	}

	public boolean isAll()
	{
		return all;
	}

	public boolean isTrivial()
	{
		return trivial;
	}

	public boolean allows( Version version )
	{
		if( all )
			return true;

		if( trivial )
			return text.equals( version.toString() );

		for( VersionRange range : ranges )
		{
			// System.out.println(range.toString() + ' ' + version + " > " +
			// range.allows(
			// version ));
			if( range.allows( version ) )
				// Logical or: it takes just one positive to be positive
				return true;
		}

		return false;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( trivial )
			return text;
		StringBuilder r = new StringBuilder();
		for( Iterator<VersionRange> i = ranges.iterator(); i.hasNext(); )
		{
			VersionRange range = i.next();
			r.append( range );
			if( i.hasNext() )
				r.append( ',' );
		}
		return r.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final boolean strict;

	private final String text;

	private final boolean trivial;

	private final boolean all;

	private final Collection<VersionRange> ranges;

	/**
	 * [\[\(]\s*([^,\s]*)\s*,\s*([^,\]\)\s]*)\s*[\]\)]
	 */
	private static Pattern PATTERN = Pattern.compile( "[\\[\\(]\\s*([^,\\s]*)\\s*,\\s*([^,\\]\\)\\s]*)\\s*[\\]\\)]" );

	private static boolean isAll( String versionSpecification )
	{
		return ( versionSpecification == null ) || versionSpecification.isEmpty() || versionSpecification.equals( "*" ) || versionSpecification.equals( "+" );
	}

	private boolean isTrivial( String versionSpecification )
	{
		char first = versionSpecification.charAt( 0 );
		if( ( first == '[' ) || ( first == '(' ) )
			return false;
		if( !isStrict() )
		{
			char last = versionSpecification.charAt( versionSpecification.length() - 1 );
			if( last == '+' )
				return false;
		}
		return true;
	}
}