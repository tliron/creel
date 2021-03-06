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

import java.util.Map;
import java.util.Objects;

/**
 * Base class for module identifiers.
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
public abstract class ModuleIdentifier implements Comparable<ModuleIdentifier>, Cloneable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *        The repository
	 */
	public ModuleIdentifier( Repository repository )
	{
		super();
		this.repository = repository;
	}

	//
	// Attributes
	//

	/**
	 * The repository
	 * 
	 * @return The repository
	 */
	public Repository getRepository()
	{
		return repository;
	}

	/**
	 * The artifacts. Note that there is no guarantee that the artifacts
	 * actually exist in the repository.
	 * 
	 * @param directories
	 *        The directories
	 * @param flat
	 *        Whether we should use a flat file structure under the root
	 *        directories (no sub-directories)
	 * @return The artifacts
	 */
	public abstract Iterable<Artifact> getArtifacts( Directories directories, boolean flat );

	//
	// Operations
	//

	/**
	 * Converts the module identifier to a config.
	 * 
	 * @return The config
	 */
	public abstract Map<String, Object> toConfig();

	//
	// Cloneable
	//

	@Override
	public abstract ModuleIdentifier clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		ModuleIdentifier moduleIdentifier = (ModuleIdentifier) object;
		Repository a = getRepository();
		Repository b = moduleIdentifier.getRepository();
		if( ( a == null ) && ( b == null ) )
			return true;
		if( ( a != null ) && ( b != null ) )
			return a.equals( b );
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getRepository() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Repository repository;
}
