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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
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
	// Constants
	//

	public static final String QUIET = "quiet";

	public static final String VERBOSITY = "verbosity";

	//
	// PreferencePage
	//

	protected Control createContents( Composite parent )
	{
		Composite top = EclipseUtil.createGrid( parent, 1, 1, true, false );

		Composite quietComposite = EclipseUtil.createGrid( top );
		quiet = new BooleanFieldEditor( QUIET, "Quiet", quietComposite );
		Composite verbosityComposite = EclipseUtil.createGrid( top );
		verbosity = new IntegerFieldEditor( VERBOSITY, "Verbosity", verbosityComposite );

		addFieldEditor( quiet );
		addFieldEditor( verbosity );

		return top;
	}

	//
	// IWorkbenchPreferencePage
	//

	public void init( IWorkbench workbench )
	{
		setPreferenceStore( Plugin.instance.getPreferenceStore() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private BooleanFieldEditor quiet;

	private IntegerFieldEditor verbosity;
}
