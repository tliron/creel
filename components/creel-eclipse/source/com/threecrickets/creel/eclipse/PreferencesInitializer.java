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

package com.threecrickets.creel.eclipse;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializes default preferences for the Creel plugin.
 * 
 * @author Tal Liron
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer
{
	//
	// AbstractPreferenceInitializer
	//

	public void initializeDefaultPreferences()
	{
		IPreferenceStore preferences = Plugin.instance.getPreferenceStore();
		preferences.setValue( PreferencesPage.QUIET, false );
		preferences.setValue( PreferencesPage.VERBOSITY, 1 );
	}
}
