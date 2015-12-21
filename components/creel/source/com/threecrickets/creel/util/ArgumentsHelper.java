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

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Tal Liron
 */
public class ArgumentsHelper
{
	//
	// Construction
	//

	public ArgumentsHelper( String[] arguments )
	{
		this.arguments = arguments;
	}

	//
	// Attributes
	//

	public boolean hasSwitch( String longForm, String shortForm )
	{
		longForm = "--" + longForm;
		shortForm = "-" + shortForm;
		for( String argument : arguments )
			if( argument.equals( longForm ) || argument.equals( shortForm ) )
				return true;
		return false;
	}

	public String getString( String longForm, String shortForm, String defaultValue )
	{
		longForm = "--" + longForm + "=";
		shortForm = "-" + shortForm;
		for( Iterator<String> i = Arrays.asList( arguments ).iterator(); i.hasNext(); )
		{
			String argument = i.next();
			if( argument.startsWith( longForm ) )
				return argument.substring( longForm.length() );
			else if( argument.startsWith( shortForm ) )
				if( i.hasNext() )
					return i.next();
		}
		return defaultValue;
	}

	public Integer getInt( String longForm, String shortForm, Integer defaultValue )
	{
		String value = getString( longForm, shortForm, null );
		return value != null ? new Integer( value ) : defaultValue;
	}

	public Boolean getBoolean( String longForm, String shortForm, Boolean defaultValue )
	{
		String value = getString( longForm, shortForm, null );
		return value != null ? new Boolean( value ) : defaultValue;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String[] arguments;
}
