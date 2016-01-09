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

package com.threecrickets.creel.eclipse.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;

/**
 * Base class for preference pages that have fields.
 * <p>
 * See <a href=
 * "http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html">
 * the Eclipse documentation</a>.
 * 
 * @author Tal Liron
 */
public abstract class PreferencePageWithFields extends PreferencePage
{
	//
	// PreferencePage
	//

	@Override
	public boolean performOk()
	{
		for( FieldEditor fieldEditor : fieldEditors )
			fieldEditor.store();
		return super.performOk();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void performDefaults()
	{
		for( FieldEditor fieldEditor : fieldEditors )
			fieldEditor.loadDefault();
		super.performDefaults();
	}

	protected void addField( FieldEditor fieldEditor )
	{
		fieldEditors.add( fieldEditor );
		fieldEditor.setPage( this );
		fieldEditor.setPreferenceStore( getPreferenceStore() );
		fieldEditor.load();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Collection<FieldEditor> fieldEditors = new ArrayList<FieldEditor>();
}
