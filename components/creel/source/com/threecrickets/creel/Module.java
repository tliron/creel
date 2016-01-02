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

package com.threecrickets.creel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a module. Modules are specified by a {@link ModuleSpecification},
 * and may optionally have a {@link ModuleIdentifier}, meaning that they have
 * been identified. A module can have one or more dependencies (modules that it
 * needs) as well as one or more supplicants (modules that have this module as a
 * dependency).
 * 
 * @author Tal Liron
 */
public class Module
{
	//
	// Construction
	//

	public Module( boolean explicit, ModuleIdentifier identifier, ModuleSpecification specification )
	{
		this.explicit = explicit;
		this.identifier = identifier;
		this.specification = specification;
	}

	//
	// Attributes
	//

	public boolean isExplicit()
	{
		return explicit;
	}

	public void setExplicit( boolean explicit )
	{
		this.explicit = explicit;
	}

	public ModuleIdentifier getIdentifier()
	{
		return identifier;
	}

	public ModuleSpecification getSpecification()
	{
		return specification;
	}

	public Iterable<Module> getDependencies()
	{
		return Collections.unmodifiableCollection( dependencies );
	}

	public Iterable<Module> getSupplicants()
	{
		return Collections.unmodifiableCollection( supplicants );
	}

	//
	// Operations
	//

	public void addDependency( Module dependency )
	{
		dependencies.add( dependency );
	}

	/**
	 * Adds a new supplicant if we don't have it already.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public void addSupplicant( Module module )
	{
		boolean found = false;
		for( Module supplicant : getSupplicants() )
			if( module.getIdentifier().equals( supplicant.getIdentifier() ) )
			{
				found = true;
				break;
			}
		if( !found )
			supplicants.add( module );
	}

	/**
	 * Removes a supplicant if we have it.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public void removeSupplicant( Module module )
	{
		for( ListIterator<Module> i = supplicants.listIterator(); i.hasNext(); )
		{
			Module supplicant = i.next();
			if( module.getIdentifier().equals( supplicant.getIdentifier() ) )
			{
				i.remove();
				break;
			}
		}
	}

	/**
	 * Copies identifier, repository, and dependencies from another module.
	 * 
	 * @param module
	 *        The other module
	 */
	public void copyIdentificationFrom( Module module )
	{
		identifier = module.getIdentifier().clone();
		dependencies.clear();
		for( Module dependency : module.getDependencies() )
			dependencies.add( dependency );
	}

	/**
	 * Adds all supplicants of another module, and makes us explicit if the
	 * other module is explicit.
	 * 
	 * @param module
	 *        The other module
	 */
	public void mergeSupplicants( Module module )
	{
		if( module.isExplicit() )
			setExplicit( true );
		for( Module supplicant : module.getSupplicants() )
			addSupplicant( supplicant );
	}

	public void replaceModule( Module oldModule, Module newModule, boolean recursive )
	{
		removeSupplicant( oldModule );
		for( ListIterator<Module> i = dependencies.listIterator(); i.hasNext(); )
		{
			Module dependency = i.next();
			if( oldModule.getIdentifier().equals( dependency.getIdentifier() ) )
			{
				i.set( newModule );
				newModule.addSupplicant( this );
			}

			if( recursive )
				dependency.replaceModule( oldModule, newModule, true );
		}
	}

	public String toString( boolean longForm )
	{
		StringBuilder r = new StringBuilder(), prefix = new StringBuilder();
		if( getIdentifier() != null )
		{
			r.append( "id=" );
			r.append( getIdentifier() );
		}
		if( ( longForm || !( getIdentifier() == null ) ) && ( getSpecification() != null ) )
		{
			if( r.length() != 0 )
				r.append( ", " );
			r.append( "spec=" );
			r.append( getSpecification() );
		}
		if( longForm )
		{
			prefix.append( isExplicit() ? '*' : '+' ); // explicit?
			prefix.append( getIdentifier() != null ? '!' : '?' ); // identified?
			if( getDependencies().iterator().hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "dependencies=" );
				int size = 0;
				for( Iterator<?> i = getDependencies().iterator(); i.hasNext(); )
					size++;
				r.append( size );
			}
			if( getSupplicants().iterator().hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "supplicants=" );
				int size = 0;
				for( Iterator<?> i = getSupplicants().iterator(); i.hasNext(); )
					size++;
				r.append( size );
			}
		}
		if( prefix.length() != 0 )
		{
			r.insert( 0, ' ' );
			r.insert( 0, prefix );
		}
		return r.toString();
	}

	//
	// Objects
	//

	@Override
	public String toString()
	{
		return toString( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private boolean explicit;

	private volatile ModuleIdentifier identifier;

	private volatile ModuleSpecification specification;

	private final List<Module> dependencies = new CopyOnWriteArrayList<Module>();

	private final List<Module> supplicants = new CopyOnWriteArrayList<Module>();
}
