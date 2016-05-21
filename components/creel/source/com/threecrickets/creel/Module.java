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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.threecrickets.creel.util.ConfigHelper;

/**
 * Represents information about a module.
 * <p>
 * Modules are specified by a {@link ModuleSpecification}, and may optionally
 * have a {@link ModuleIdentifier}, meaning that they have been identified.
 * <p>
 * A module can have one or more dependencies (modules that it needs) as well as
 * one or more supplicants (modules that have this module as a dependency).
 * <p>
 * An "explicit" module is one that was explicitly listed as a dependency. An
 * "implicit" module is a dependency of another module. So, explicit modules
 * represent the roots of the dependency tree.
 * <p>
 * Note that an explicit module can still have supplicants: it could have been
 * explicitly listed and <i>also</i> listed as a dependency of another module.
 * 
 * @author Tal Liron
 */
public class Module
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param explicit
	 *        Whether the module is explicit
	 * @param identifier
	 *        The identifier or null
	 * @param specification
	 *        The specification
	 */
	public Module( boolean explicit, ModuleIdentifier identifier, ModuleSpecification specification )
	{
		this.explicit = explicit;
		this.identifier = identifier;
		this.specification = specification;
	}

	/**
	 * Config constructor.
	 * <p>
	 * Supports identifiers, but not specifications; supplicants, but not
	 * dependents.
	 * 
	 * @param config
	 *        The config
	 * @param factory
	 *        The factory
	 */
	public Module( Map<String, ?> config, Factory factory )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		explicit = configHelper.getBoolean( "explicit" );
		Map<String, Object> identifierConfig = configHelper.getSubConfig( "identifier." );
		String platform = new ConfigHelper( identifierConfig ).getString( "platform" );
		identifier = factory.newModuleIdentifier( platform, identifierConfig );
		for( Map<String, Object> supplicantConfig : configHelper.getSubConfigs( "supplicant." ) )
		{
			platform = new ConfigHelper( supplicantConfig ).getString( "platform" );
			ModuleIdentifier supplicantIdentifier = factory.newModuleIdentifier( platform, supplicantConfig );
			supplicants.add( new Module( false, supplicantIdentifier, null ) );
		}
	}

	//
	// Attributes
	//

	/**
	 * Whether the module is explicit
	 * 
	 * @return True if explicit
	 */
	public boolean isExplicit()
	{
		return explicit;
	}

	/**
	 * Whether the module is explicit
	 * 
	 * @param explicit
	 *        True if explicit
	 */
	public void setExplicit( boolean explicit )
	{
		this.explicit = explicit;
	}

	/**
	 * The module identifier.
	 * 
	 * @return The module identifier or null
	 */
	public ModuleIdentifier getIdentifier()
	{
		return identifier;
	}

	/**
	 * The module specification
	 * 
	 * @return The module specification
	 */
	public ModuleSpecification getSpecification()
	{
		return specification;
	}

	/**
	 * The module's dependencies.
	 * 
	 * @return The dependencies
	 */
	public synchronized Iterable<Module> getDependencies()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( dependencies ) );
	}

	/**
	 * The module's supplicants.
	 * 
	 * @return The supplicants
	 */
	public synchronized Iterable<Module> getSupplicants()
	{
		return Collections.unmodifiableCollection( new ArrayList<Module>( supplicants ) );
	}

	//
	// Operations
	//

	/**
	 * Converts the module to a config.
	 * <p>
	 * Supports identifiers, but not specifications; supplicants, but not
	 * dependents.
	 * 
	 * @return The config
	 */
	public Map<String, Object> toConfig()
	{
		Map<String, Object> config = new HashMap<String, Object>();
		if( isExplicit() )
			config.put( "explicit", true );
		for( Map.Entry<String, Object> entry : getIdentifier().toConfig().entrySet() )
			config.put( "identifier." + entry.getKey(), entry.getValue() );
		int i = 0;
		for( Module supplicant : getSupplicants() )
		{
			for( Map.Entry<String, Object> entry : supplicant.getIdentifier().toConfig().entrySet() )
				config.put( "supplicant." + i + '.' + entry.getKey(), entry.getValue() );
			i++;
		}
		return Collections.unmodifiableMap( config );
	}

	/**
	 * Sets another module as a dependency of this module. Makes sure that
	 * duplicate dependencies are not added.
	 * 
	 * @param module
	 *        The dependency module
	 */
	public synchronized void addDependency( Module module )
	{
		boolean found = false;
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		if( moduleIdentifier != null )
			for( Module dependency : getDependencies() )
				if( moduleIdentifier.equals( dependency.getIdentifier() ) )
				{
					found = true;
					break;
				}
		if( !found )
			dependencies.add( module );
	}

	/**
	 * Sets another module as a supplicant of this module. Makes sure that
	 * duplicate supplicants are not added.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public synchronized void addSupplicant( Module module )
	{
		boolean found = false;
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		for( Module supplicant : getSupplicants() )
			if( moduleIdentifier.equals( supplicant.getIdentifier() ) )
			{
				found = true;
				break;
			}
		if( !found )
			supplicants.add( module );
	}

	/**
	 * Sets another module to not be a supplicant of this module.
	 * 
	 * @param module
	 *        The supplicant module
	 */
	public synchronized void removeSupplicant( Module module )
	{
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		for( ListIterator<Module> i = supplicants.listIterator(); i.hasNext(); )
		{
			Module supplicant = i.next();
			if( moduleIdentifier.equals( supplicant.getIdentifier() ) )
			{
				i.remove();
				break;
			}
		}
	}

	/**
	 * Copies identifier, repository, and dependencies from another module
	 * instance.
	 * 
	 * @param module
	 *        The other module
	 */
	public synchronized void copyIdentificationFrom( Module module )
	{
		identifier = module.getIdentifier().clone();
		dependencies.clear();
		for( Module dependency : module.getDependencies() )
			addDependency( dependency );
	}

	/**
	 * Adds all supplicants of another module, and makes us explicit if the
	 * other module is explicit.
	 * 
	 * @param module
	 *        The other module
	 */
	public synchronized void mergeSupplicants( Module module )
	{
		if( module.isExplicit() )
			setExplicit( true );
		for( Module supplicant : module.getSupplicants() )
			addSupplicant( supplicant );
	}

	/**
	 * Replaces a module with another one in the dependency tree.
	 * 
	 * @param oldModule
	 *        The old module
	 * @param newModule
	 *        The new module
	 * @param recursive
	 *        True if we should recurse replacing in dependencies
	 */
	public synchronized void replaceModule( Module oldModule, Module newModule, boolean recursive )
	{
		removeSupplicant( oldModule );
		ModuleIdentifier oldModuleIdentifier = oldModule.getIdentifier();
		for( ListIterator<Module> i = dependencies.listIterator(); i.hasNext(); )
		{
			Module dependency = i.next();
			if( oldModuleIdentifier.equals( dependency.getIdentifier() ) )
			{
				dependency = newModule;
				i.set( dependency );
				dependency.addSupplicant( this );
			}

			if( recursive )
				dependency.replaceModule( oldModule, newModule, true );
		}
	}

	/**
	 * Represents the module as a string.
	 * 
	 * @param longForm
	 *        True to use the long form
	 * @return The string representation
	 */
	public String toString( boolean longForm )
	{
		StringBuilder r = new StringBuilder(), prefix = new StringBuilder();
		ModuleIdentifier moduleIdentifier = getIdentifier();
		ModuleSpecification moduleSpecification = getSpecification();
		if( moduleIdentifier != null )
		{
			r.append( "id=" );
			r.append( moduleIdentifier );
		}
		if( ( longForm || ( moduleIdentifier != null ) ) && ( moduleSpecification != null ) )
		{
			if( r.length() != 0 )
				r.append( ", " );
			r.append( "spec=" );
			r.append( moduleSpecification );
		}
		if( longForm )
		{
			prefix.append( isExplicit() ? '*' : '+' ); // explicit?
			prefix.append( moduleIdentifier != null ? '!' : '?' ); // identified?
			Iterator<?> i = getDependencies().iterator();
			if( i.hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "dependencies=" );
				int size = 0;
				for( ; i.hasNext(); i.next() )
					size++;
				r.append( size );
			}
			i = getSupplicants().iterator();
			if( i.hasNext() )
			{
				if( r.length() != 0 )
					r.append( ", " );
				r.append( "supplicants=" );
				int size = 0;
				for( ; i.hasNext(); i.next() )
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

	private volatile boolean explicit;

	private volatile ModuleIdentifier identifier;

	private volatile ModuleSpecification specification;

	private final List<Module> dependencies = new ArrayList<Module>();

	private final List<Module> supplicants = new ArrayList<Module>();
}
