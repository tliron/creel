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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.eclipse.internal.Text;

/**
 * Command to add the Creel {@link Nature} to the selected project.
 * <p>
 * Gives the user the option to use a "creel.properties" file, or to manage
 * Creel on their own (for example with Ant).
 * 
 * @author Tal Liron
 */
public class AddNatureCommand extends AbstractHandler
{
	//
	// AbstractHandler
	//

	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		ISelection selection = HandlerUtil.getCurrentSelection( event );

		try
		{
			for( IProject project : EclipseUtil.getSelectedProjects( selection, false, Nature.ID ) )
			{
				if( confirm( project ) )
				{
					EclipseUtil.addNature( project, Nature.ID );
					Plugin.getSimpleLog().log( IStatus.INFO, "Added Creel nature to: " + project );
				}
			}
		}
		catch( CoreException x )
		{
			Plugin.getSimpleLog().log( IStatus.ERROR, x );
		}
		catch( IOException x )
		{
			Plugin.getSimpleLog().log( IStatus.ERROR, x );
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static boolean confirm( IProject project ) throws CoreException, IOException
	{
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final Shell dialog = new Shell( shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
		dialog.setText( Text.AddNatureTitle );
		FillLayout layout = new FillLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		dialog.setLayout( layout );

		Composite main = new Composite( dialog, SWT.NO_BACKGROUND );
		main.setLayout( new GridLayout( 1, false ) );

		final AtomicBoolean isUseConfigurationFile = new AtomicBoolean( true );

		Button useConfigurationFile = new Button( main, SWT.RADIO );
		useConfigurationFile.setText( Builder.hasDefaultConfigurationFile( project ) ? Text.AddNatureUseExistingConfiguration : Text.AddNatureCreateConfiguration );
		useConfigurationFile.setSelection( true );
		useConfigurationFile.addListener( SWT.Selection, new Listener()
		{
			public void handleEvent( Event event )
			{
				isUseConfigurationFile.set( true );
			}
		} );

		Button selfManaged = new Button( main, SWT.RADIO );
		selfManaged.setText( Text.AddNatureOtherwise );
		useConfigurationFile.addListener( SWT.Selection, new Listener()
		{
			public void handleEvent( Event event )
			{
				isUseConfigurationFile.set( false );
			}
		} );

		Composite buttons = new Composite( main, SWT.NO_BACKGROUND );
		buttons.setLayoutData( new GridData( SWT.END, SWT.CENTER, true, false ) );
		buttons.setLayout( new FillLayout() );

		final AtomicBoolean add = new AtomicBoolean();
		Button addButton = new Button( buttons, SWT.PUSH );
		addButton.setText( Text.AddNatureAdd );
		addButton.addListener( SWT.Selection, new Listener()
		{
			public void handleEvent( Event event )
			{
				add.set( true );
				dialog.dispose();
			}
		} );

		Button cancelButton = new Button( buttons, SWT.PUSH );
		cancelButton.setText( Text.AddNatureCancel );
		cancelButton.addListener( SWT.Selection, new Listener()
		{
			public void handleEvent( Event event )
			{
				dialog.dispose();
			}
		} );

		dialog.pack();
		dialog.open();
		EclipseUtil.waitUntilDisposed( dialog );

		if( !add.get() )
			return false;

		if( isUseConfigurationFile.get() )
			Builder.ensureDefaultConfiguration( project );

		return true;
	}
}
