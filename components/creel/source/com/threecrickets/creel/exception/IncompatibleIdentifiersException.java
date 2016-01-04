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

package com.threecrickets.creel.exception;

import com.threecrickets.creel.ModuleIdentifier;

/**
 * Thrown when two module identifiers cannot be compared.
 * 
 * @author Tal Liron
 */
public class IncompatibleIdentifiersException extends ClassCastException
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param moduleIdentifier1
	 *        The first module identifier
	 * @param moduleIdentifier2
	 *        The second module identifier
	 */
	public IncompatibleIdentifiersException( ModuleIdentifier moduleIdentifier1, ModuleIdentifier moduleIdentifier2 )
	{
		super( "Module identifiers could not be compared" );
		this.moduleIdentifier1 = moduleIdentifier1;
		this.moduleIdentifier2 = moduleIdentifier2;
	}

	//
	// Attributes
	//

	/**
	 * The first module identifier.
	 * 
	 * @return The first module identifier
	 */
	public ModuleIdentifier getModuleIdentifier1()
	{
		return moduleIdentifier1;
	}

	/**
	 * The second module identifier.
	 * 
	 * @return The second module identifier
	 */
	public ModuleIdentifier getModuleIdentifier2()
	{
		return moduleIdentifier2;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final ModuleIdentifier moduleIdentifier1;

	private final ModuleIdentifier moduleIdentifier2;
}
