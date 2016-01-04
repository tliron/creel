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

package com.threecrickets.creel.internal;

import java.util.ArrayList;
import java.util.Collections;

import com.threecrickets.creel.Engine;
import com.threecrickets.creel.Module;

/**
 * Conflict implementation.
 * 
 * @author Tal Liron
 */
public class Conflict extends ArrayList<Module> implements com.threecrickets.creel.Conflict
{
	//
	// Operations
	//

	/**
	 * Chooses a module according to the conflict resolution policy.
	 * 
	 * @param policy
	 *        The policy
	 */
	public void choose( Engine.ConflictPolicy policy )
	{
		if( policy == Engine.ConflictPolicy.NEWEST )
			choose( size() - 1 );
		else if( policy == Engine.ConflictPolicy.OLDEST )
			choose( 0 );
	}

	/**
	 * Chooses a module.
	 * 
	 * @param index
	 *        The index
	 */
	public void choose( int index )
	{
		chosen = remove( index );

		// Merge all supplicants into chosen module
		for( Module module : this )
			chosen.mergeSupplicants( module );
	}

	/**
	 * Sorts the module identifiers.
	 */
	public void sort()
	{
		Collections.sort( this, ModuleIdentifierComparator.INSTANCE );
	}

	//
	// Conflict
	//

	public Module getChosen()
	{
		return chosen;
	}

	public Iterable<Module> getRejects()
	{
		return Collections.unmodifiableCollection( this );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private Module chosen;
}
