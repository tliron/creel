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

package com.threecrickets.creel.internal;

import java.util.Comparator;

import com.threecrickets.creel.Module;
import com.threecrickets.creel.exception.IncompatibleIdentifiersException;

/**
 * Compares modules according to their identifiers.
 * 
 * @author Tal Liron
 */
public class ModuleIdentifierComparator implements Comparator<Module>
{
	//
	// Constants
	//

	/**
	 * The singleton.
	 */
	public static final ModuleIdentifierComparator INSTANCE = new ModuleIdentifierComparator();

	//
	// Comparator
	//

	@Override
	public int compare( Module m1, Module m2 )
	{
		try
		{
			return m1.getIdentifier().compareTo( m2.getIdentifier() );
		}
		catch( IncompatibleIdentifiersException x )
		{
			return m1.getIdentifier().toString().compareTo( m2.getIdentifier().toString() );

		}
	}
}
