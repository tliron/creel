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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;

/**
 * Project properties page for projects with Creel nature.
 * 
 * @author Tal Liron
 */
public class ProjectProperties extends PropertyPage
{
	//
	// Construction
	//

	public ProjectProperties()
	{
		setTitle( "Creel" );
		setDescription( "Creel builder properties" );
	}

	//
	// PreferencePage
	//

	protected Control createContents( Composite parent )
	{
		Composite top = EclipseUtil.createComposite( parent, 1, 1, true, false );

		Button button = new Button( top, SWT.CHECK );
		button.setText( "HIIIIIII" );

		return top;
	}
}
