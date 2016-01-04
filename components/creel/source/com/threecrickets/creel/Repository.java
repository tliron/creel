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

import java.util.Map;
import java.util.Objects;

import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.InvalidArtifactException;
import com.threecrickets.creel.util.ConfigHelper;

/**
 * Base class for repositories.
 * <p>
 * These are implemented differently per platform.
 * <p>
 * Child classes <b>must</b> override {@link Object#equals(Object)} and
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
 * dynamic class checking in the base class, like so:
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
public abstract class Repository implements Cloneable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        The repository ID (should be unique in the engine)
	 * @param all
	 *        Whether the engine should attempt to identify all modules in this
	 *        repository
	 */
	public Repository( String id, boolean all )
	{
		this.id = id;
		this.all = all;
	}

	/**
	 * Config constructor.
	 * 
	 * @param config
	 *        The config
	 */
	public Repository( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		id = configHelper.getString( "id" );
		all = configHelper.getBoolean( "all", true );
	}

	//
	// Attributes
	//

	/**
	 * The repository ID. Should be unique in the engine.
	 * 
	 * @return The ID
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Whether the engine should attempt to identify all modules in this
	 * repository.
	 * 
	 * @return True if for all modules
	 */
	public boolean isAll()
	{
		return all;
	}

	//
	// Operations
	//

	/**
	 * Finds all modules in the repository that match the module specification.
	 * The result is sorted, such that the oldest module is first and the newest
	 * module is last.
	 * 
	 * @param moduleSpecification
	 *        The module specification
	 * @param notifier
	 *        The notifier or null
	 * @return The allowed module identifiers for the specification
	 */
	public abstract Iterable<ModuleIdentifier> getAllowedModuleIdentifiers( ModuleSpecification moduleSpecification, Notifier notifier );

	/**
	 * Checks if a module exists in the repository.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @return True is exists
	 */
	public abstract boolean hasModule( ModuleIdentifier moduleIdentifier );

	/**
	 * Fetches module information (specifically its dependencies) from the
	 * repository.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param notifier
	 *        The notifier or null
	 * @return The module or null if it doesn't exist
	 */
	public abstract Module getModule( ModuleIdentifier moduleIdentifier, Notifier notifier );

	/**
	 * Makes sure an artifact is valid. This is usually achieved by comparing
	 * the cryptographic digest of the file to one stored in the repository. If
	 * the artifact is invalid, a {@link InvalidArtifactException} will be
	 * thrown.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param artifact
	 *        The artifact
	 * @param notifier
	 *        The notifier or null
	 */
	public abstract void validateArtifact( ModuleIdentifier moduleIdentifier, Artifact artifact, Notifier notifier );

	/**
	 * A {@link Runnable} version of
	 * {@link Repository#validateArtifact(ModuleIdentifier, Artifact, Notifier)}
	 * .
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param artifact
	 *        The artifact
	 * @param notifier
	 *        The notifier or null
	 * @return The runnable
	 */
	public Runnable validateArtifactTask( ModuleIdentifier moduleIdentifier, Artifact artifact, Notifier notifier )
	{
		return new ValidateArtifact( moduleIdentifier, artifact, notifier );
	}

	/**
	 * Attempt to apply a rule to a module. If the rule is unsupported by this
	 * repository, nothing should happen. The method can optionally return a
	 * {@link Command} for the engine to process.
	 * 
	 * @param module
	 *        The module
	 * @param rule
	 *        The rule
	 * @param notifier
	 *        The notifier or null
	 * @return The command or null
	 */
	public abstract Command applyRule( Module module, Rule rule, Notifier notifier );

	//
	// Cloneable
	//

	@Override
	public abstract Repository clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		Repository repository = (Repository) object;
		return id.equals( repository.getId() ) && ( isAll() == repository.isAll() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getId(), isAll() );
	}

	@Override
	public String toString()
	{
		return "id=" + getId();
	}

	//
	// Classes
	//

	public class ValidateArtifact implements Runnable
	{
		public ValidateArtifact( ModuleIdentifier moduleIdentifier, Artifact artifact, Notifier notifier )
		{
			this.moduleIdentifier = moduleIdentifier;
			this.artifact = artifact;
			this.notifier = notifier;
		}

		public void run()
		{
			validateArtifact( moduleIdentifier, artifact, notifier );
		}

		final ModuleIdentifier moduleIdentifier;

		final Artifact artifact;

		final Notifier notifier;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String id;

	private final boolean all;
}
