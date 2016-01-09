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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.threecrickets.creel.eclipse.internal.Text;

/**
 * Descriptor for {@link ClasspathContainer}.
 * 
 * @author Tal Liron
 */
public class ClasspathContainerPage extends WizardPage implements IClasspathContainerPage
{
	//
	// Construction
	//

	public ClasspathContainerPage()
	{
		super( Text.ClasspathName, Text.ClasspathName, null );
		setDescription( Text.ClasspathDescription );
		setPageComplete( true );
		this.id = ClasspathContainer.ID;
	}

	//
	// IClasspathContainerPage
	//

	public void createControl( Composite parent )
	{
		setControl( new Composite( parent, SWT.NULL ) );
	}

	public boolean finish()
	{
		return true;
	}

	public IClasspathEntry getSelection()
	{
		return JavaCore.newContainerEntry( id );
	}

	public void setSelection( IClasspathEntry containerEntry )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final IPath id;
}
