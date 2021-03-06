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

package com.threecrickets.creel.eclipse.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Text strings loaded from a resource bundle.
 * 
 * @author Tal Liron
 */
public abstract class Text extends NLS
{
	//
	// Constants
	//

	public static String ClasspathName;

	public static String ClasspathDescription;

	public static String ClasspathRefresh;

	public static String AddNatureTitle;

	public static String AddNatureUseExistingScript;

	public static String AddNatureCreateScript;

	public static String AddNatureUseExistingConfiguration;

	public static String AddNatureCreateConfiguration;

	public static String AddNatureOtherwise;

	public static String AddNatureAdd;

	public static String AddNatureCancel;

	public static String PropertiesTitle;

	public static String PropertiesDescription;

	public static String PropertiesScriptFile;

	public static String PropertiesScriptEngine;

	public static String PropertiesConfigurationFile;

	public static String PropertiesDefaultDir;

	public static String PropertiesLibraryDir;

	public static String PropertiesApiDir;

	public static String PropertiesSourceDir;

	public static String PropertiesStateFile;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	static
	{
		NLS.initializeMessages( Text.class.getPackage().getName() + ".text", Text.class );
	}

	private Text()
	{
	}
}
