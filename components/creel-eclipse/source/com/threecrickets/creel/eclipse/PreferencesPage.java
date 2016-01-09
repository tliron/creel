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

package com.threecrickets.creel.eclipse;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.eclipse.internal.PreferencePageWithFields;

/**
 * General preferences for the Creel plugin.
 * 
 * @author Tal Liron
 */
public class PreferencesPage extends PreferencePageWithFields implements IWorkbenchPreferencePage
{
	//
	// PreferencePage
	//

	protected Control createContents( Composite parent )
	{
		Composite top = EclipseUtil.createComposite( parent, 1, 1, true, false );
		return top;
	}

	//
	// IWorkbenchPreferencePage
	//

	public void init( IWorkbench workbench )
	{
		setPreferenceStore( Plugin.getDefault().getPreferenceStore() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private
}
