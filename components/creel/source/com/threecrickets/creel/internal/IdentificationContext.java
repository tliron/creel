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

import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.creel.Repository;

/**
 * Used in the identification phase of the engine.
 * 
 * @author Tal Liron
 */
public class IdentificationContext
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param repositories
	 *        The repositories
	 * @param recursive
	 *        Whether we are recursing into the dependencies
	 */
	public IdentificationContext( Iterable<Repository> repositories, boolean recursive )
	{
		for( Repository repository : repositories )
			if( repository.isAll() )
				this.repositories.add( repository );
		this.recursive = recursive;
	}

	//
	// Attributes
	//

	/**
	 * The repositories.
	 * 
	 * @return The repositories
	 */
	public Collection<Repository> getRepositories()
	{
		return repositories;
	}

	/**
	 * Whether we should exclude the current module.
	 * 
	 * @return True to exclude
	 */
	public boolean isExclude()
	{
		return exclude;
	}

	/**
	 * Whether we should exclude the current module.
	 * 
	 * @param exclude
	 *        True to exclude
	 */
	public void setExclude( boolean exclude )
	{
		this.exclude = exclude;
	}

	/**
	 * Whether we are recursing into the dependencies.
	 * 
	 * @return True to recurse
	 */
	public boolean isRecursive()
	{
		return recursive;
	}

	/**
	 * Whether we are recursing into the dependencies.
	 * 
	 * @param recursive
	 *        True to recurse
	 */
	public void setRecursive( boolean recursive )
	{
		this.recursive = recursive;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Collection<Repository> repositories = new ArrayList<Repository>();

	private boolean exclude;

	private boolean recursive;
}
