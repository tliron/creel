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

package com.threecrickets.creel.maven.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed Maven version specification.
 * <p>
 * Supports "wildcard" specifications (null, "*", or "+") meaning that all
 * versions would match the specification.
 * 
 * @author Tal Liron
 */
public class VersionSpecification
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param versionSpecification
	 *        The version specification
	 * @param strict
	 *        Whether we are in strict Maven mode
	 */
	public VersionSpecification( String versionSpecification, boolean strict )
	{
		this.strict = strict;
		versionSpecification = versionSpecification == null ? "" : versionSpecification.trim();
		text = versionSpecification;

		// Check if wildcard
		if( isWildcard( versionSpecification ) )
		{
			wildcard = true;
			trivial = false;
			ranges = null;
			return;
		}

		// Check if trivial
		if( isTrivial( versionSpecification ) )
		{
			wildcard = false;
			trivial = true;
			ranges = null;
			return;
		}

		// TODO: regexp match to see if it's parseable

		wildcard = false;
		trivial = false;
		ranges = new ArrayList<VersionRange>();

		// Convert Ivy/Gradle range suffix to Maven: "1.0+" to "[1.0,)"
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
			 * TODO: if (!matcher.find()) { // Make sure there is a comma in
			 * between ranges var between = version.substring(lastIndex,
			 * matches.index); if (!/^\s+,\s+$/.test(between)) return null; }
			 */
		}
	}

	//
	// Attributes
	//

	/**
	 * Whether we are in strict Maven mode.
	 * 
	 * @return True if strict
	 */
	public boolean isStrict()
	{
		return strict;
	}

	/**
	 * Whether we allow for all versions.
	 * 
	 * @return True if wildcard
	 */
	public boolean isWildcard()
	{
		return wildcard;
	}

	/**
	 * Whether we are a trivial version (no ranges).
	 * 
	 * @return True if trivial
	 */
	public boolean isTrivial()
	{
		return trivial;
	}

	/**
	 * Check whether the version is allowed by this version specification.
	 * 
	 * @param version
	 *        The version
	 * @return True if allowed
	 */
	public boolean allows( Version version )
	{
		if( isWildcard() )
			return true;

		if( isTrivial() )
			return text.equals( version.getText() );

		for( VersionRange range : ranges )
			if( range.in( version ) )
				// Logical or: it takes just one positive to be positive
				return true;

		return false;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( isTrivial() )
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

	private final boolean wildcard;

	private final Collection<VersionRange> ranges;

	/**
	 * [\[\(]\s*([^,\s]*)\s*,\s*([^,\]\)\s]*)\s*[\]\)]
	 */
	private static Pattern PATTERN = Pattern.compile( "[\\[\\(]\\s*([^,\\s]*)\\s*,\\s*([^,\\]\\)\\s]*)\\s*[\\]\\)]" );

	private static boolean isWildcard( String versionSpecification )
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
