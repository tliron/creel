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

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.eclipse.internal.Text;

/**
 * Properties page for {@link Classpath}.
 * 
 * @author Tal Liron
 */
public class ClasspathPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public ClasspathPage()
	{
		super( Text.ClasspathName, Text.ClasspathName, null );
		setDescription( Text.ClasspathDescription );
	}

	//
	// IClasspathContainerPage
	//

	public void createControl( Composite parent )
	{
		Composite top = EclipseUtil.createGrid( parent, 1, 1, true, false );
		Button refresh = new Button( top, SWT.PUSH );
		refresh.setText( Text.ClasspathRefresh );
		refresh.addListener( SWT.Selection, new Listener()
		{
			public void handleEvent( Event event )
			{
				try
				{
					EclipseUtil.setClasspathContainer( project, new Classpath( project.getProject() ) );
				}
				catch( JavaModelException x )
				{
					throw new RuntimeException( x );
				}
			}
		} );
		setControl( top );
	}

	public boolean finish()
	{
		entry = JavaCore.newContainerEntry( Classpath.PATH );
		return true;
	}

	public IClasspathEntry getSelection()
	{
		return entry;
	}

	public void setSelection( IClasspathEntry entry )
	{
		this.entry = entry;
	}

	//
	// IClasspathContainerPageExtension
	//

	public void initialize( IJavaProject project, IClasspathEntry[] entries )
	{
		this.project = project;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile IJavaProject project;

	private volatile IClasspathEntry entry;
}
