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

/**
 * Represents a resolved module identification conflict.
 * 
 * @author Tal Liron
 */
public interface Conflict
{
	//
	// Attributes
	//

	/**
	 * The chosen module.
	 * 
	 * @return The chosen module
	 */
	public Module getChosen();

	/**
	 * The rejected modules. There will be at least one.
	 * 
	 * @return The rejected modules
	 */
	public Iterable<Module> getRejects();
}
