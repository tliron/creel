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

package com.threecrickets.creel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Base class for module specifications.
 * <p>
 * These are implemented differently per platform.
 * <p>
 * Child classes must have a config constructor (accepting a Map&lt;String,
 * ?$gt;). Additional, they must override {@link Object#equals(Object)} and
 * {@link Object#hashCode()} with a proper implementation. The following
 * semantics are supported and recommended:
 * 
 * <pre>
 * &#64;Override
 * public boolean equals(Object object) {
 * 	if(!super.equals(object)) return false;
 * 	MyClass myObject = (MyClass) object;
 * 	return ...;
 * }
 * 
 * &#64;Override
 * public int hashCode() {
 * 	return Objects.hash(super.hashCode(), ...);
 * </pre>
 * 
 * Note that for the equals() override to work this way, we had to implement
 * dynamic class checking in the base class, like so:<br>
 * <br>
 * 
 * <pre>
 * if((object == null) || (getClass() != object.getClass())) return false;
 * ...
 * </pre>
 * 
 * Likewise, we made sure that hashCode() in the base class properly hashes our
 * data fields, and never returns an arbitrary number in case there are no data
 * fields.
 * 
 * @author Tal Liron
 */
public abstract class ModuleSpecification implements Cloneable
{
	/**
	 * Checks whether a module identifier is allowed by the specification.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @return True if allowed
	 */
	public abstract boolean allowsModuleIdentifier( ModuleIdentifier moduleIdentifier );

	/**
	 * Filters out those module identifiers that match the specification.
	 * 
	 * @param moduleIdentifiers
	 *        The module identifiers
	 * @return The filtered module identifiers
	 */
	public Iterable<ModuleIdentifier> filterAllowedModuleIdentifiers( Iterable<ModuleIdentifier> moduleIdentifiers )
	{
		Collection<ModuleIdentifier> allowedModuleIdentifiers = new ArrayList<ModuleIdentifier>();
		for( ModuleIdentifier moduleIdentifier : moduleIdentifiers )
			if( allowsModuleIdentifier( moduleIdentifier ) )
				allowedModuleIdentifiers.add( moduleIdentifier );
		return Collections.unmodifiableCollection( allowedModuleIdentifiers );
	}

	//
	// Cloneable
	//

	@Override
	public abstract ModuleSpecification clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash();
	}
}
