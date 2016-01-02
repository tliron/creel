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
import java.util.Iterator;
import java.util.List;

import com.threecrickets.creel.Module;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;

/**
 * @author Tal Liron
 */
public class Modules implements Iterable<Module>
{
	//
	// Attributes
	//

	public synchronized int size()
	{
		return modules.size();
	}

	//
	// Operations
	//

	public synchronized Module get( ModuleIdentifier moduleIdentifier )
	{
		for( Module module : modules )
			if( moduleIdentifier.equals( module.getIdentifier() ) )
				return module;
		return null;
	}

	public synchronized Module get( ModuleSpecification moduleSpecification )
	{
		for( Module module : modules )
			if( moduleSpecification.equals( module.getSpecification() ) )
				return module;
		return null;
	}

	public synchronized void addByIdentifier( Module module )
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
	}

	public synchronized void addBySpecification( Module module )
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
	}

	public synchronized void remove( ModuleIdentifier moduleIdentifier )
	{
		for( Module module : modules )
			if( moduleIdentifier.equals( module.getIdentifier() ) )
			{
				modules.remove( module );
				break;
			}
	}

	public synchronized void remove( ModuleSpecification moduleSpecification )
	{
		for( Module module : modules )
			if( moduleSpecification.equals( module.getSpecification() ) )
			{
				modules.remove( module );
				break;
			}
	}

	public synchronized void sortByIdentifiers()
	{
		Collections.sort( modules, ModuleIdentifierComparator.INSTANCE );
	}

	public synchronized void sortBySpecifications()
	{
		Collections.sort( modules, ModuleSpecificationComparator.INSTANCE );
	}

	//
	// Iterable
	//

	@Override
	public synchronized Iterator<Module> iterator()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( modules ) ).iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Module> modules = new ArrayList<Module>();
}
