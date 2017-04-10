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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.threecrickets.creel.Module;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;

/**
 * Manages a thread-safe collection of unique modules.
 * 
 * @author Tal Liron
 */
public class Modules implements Iterable<Module>
{
	//
	// Attributes
	//

	/**
	 * Number of modules in collection.
	 * 
	 * @return The number of modules
	 */
	public synchronized int size()
	{
		return modules.size();
	}

	//
	// Operations
	//

	/**
	 * Gets a module according to its identifier.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @return The module or null if not in collection
	 */
	public synchronized Module get( ModuleIdentifier moduleIdentifier )
	{
		for( Module module : modules )
			if( moduleIdentifier.equals( module.getIdentifier() ) )
				return module;
		return null;
	}

	/**
	 * Gets a module according to its specification.
	 * 
	 * @param moduleSpecification
	 *        The module specification
	 * @return The module or null if not in collection
	 */
	public synchronized Module get( ModuleSpecification moduleSpecification )
	{
		for( Module module : modules )
			if( moduleSpecification.equals( module.getSpecification() ) )
				return module;
		return null;
	}

	/**
	 * Adds a module, making sure its identifier only appears once in the
	 * collection.
	 * 
	 * @param module
	 *        The module
	 * @return True if added, false is the identifier is already in the
	 *         collection
	 */
	public synchronized boolean addByIdentifier( Module module )
	{
		boolean found = false;
		for( Module aModule : modules )
			if( module.getIdentifier().equals( aModule.getIdentifier() ) )
			{
				aModule.mergeSupplicants( module );
				found = true;
				break;
			}
		if( !found )
			modules.add( module );
		return !found;
	}

	/**
	 * Adds a module, making sure its specification only appears once in the
	 * collection.
	 * 
	 * @param module
	 *        The module
	 * @return True if added, false is the specification is already in the
	 *         collection
	 */
	public synchronized boolean addBySpecification( Module module )
	{
		boolean found = false;
		for( Module aModule : modules )
			if( module.getSpecification().equals( aModule.getSpecification() ) )
			{
				aModule.mergeSupplicants( module );
				found = true;
				break;
			}
		if( !found )
			modules.add( module );
		return !found;
	}

	/**
	 * Removes a module according to its identifier.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 */
	public synchronized void remove( ModuleIdentifier moduleIdentifier )
	{
		for( Module module : modules )
			if( moduleIdentifier.equals( module.getIdentifier() ) )
			{
				modules.remove( module );
				break;
			}
	}

	/**
	 * Removes a module according to its specification.
	 * 
	 * @param moduleSpecification
	 *        The module specification
	 */
	public synchronized void remove( ModuleSpecification moduleSpecification )
	{
		for( Module module : modules )
			if( moduleSpecification.equals( module.getSpecification() ) )
			{
				modules.remove( module );
				break;
			}
	}

	/**
	 * Sorts the collection by identifiers as strings.
	 */
	public synchronized void sortByIdentifiers()
	{
		Collections.sort( modules, ModuleIdentifierComparator.INSTANCE );
	}

	/**
	 * Sorts the collection by specifications as strings.
	 */
	public synchronized void sortBySpecifications()
	{
		Collections.sort( modules, ModuleSpecificationComparator.INSTANCE );
	}

	//
	// Iterable
	//

	public synchronized Iterator<Module> iterator()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( modules ) ).iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Module> modules = new ArrayList<Module>();
}
