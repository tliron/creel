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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;
import com.threecrickets.creel.eclipse.internal.Text;

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

	/**
	 * Constructor.
	 */
	public ProjectProperties()
	{
		setTitle( Text.PropertiesTitle );
		setDescription( Text.PropertiesDescription );
	}

	//
	// PreferencePage
	//

	@Override
	public void performApply()
	{
		IProject project = getProject();
		if( project == null )
			return;

		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put( Builder.SCRIPT_FILE_ARGUMENT, scriptFile.getText() );
		arguments.put( Builder.SCRIPT_ENGINE_ARGUMENT, scriptEngine.getText() );
		arguments.put( Builder.CONFIGURATION_FILE_ARGUMENT, configurationFile.getText() );
		arguments.put( Builder.DEFAULT_DIR_ARGUMENT, defaultDir.getText() );
		arguments.put( Builder.LIBRARY_DIR_ARGUMENT, libraryDir.getText() );
		arguments.put( Builder.API_DIR_ARGUMENT, apiDir.getText() );
		arguments.put( Builder.SOURCE_DIR_ARGUMENT, sourceDir.getText() );
		arguments.put( Builder.STATE_FILE_ARGUMENT, stateFile.getText() );

		try
		{
			EclipseUtil.setBuilder( project, Builder.ID, arguments );
		}
		catch( CoreException x )
		{
			throw new RuntimeException( x );
		}
	}

	@Override
	public void performDefaults()
	{
		IProject project = getProject();
		if( project == null )
			return;

		try
		{
			Map<String, String> arguments = Builder.getDefaultArguments( project );
			EclipseUtil.setBuilder( project, Builder.ID, arguments );
		}
		catch( CoreException x )
		{
			throw new RuntimeException( x );
		}
	}

	protected Control createContents( Composite parent )
	{
		IProject project = getProject();
		if( project == null )
			return parent;

		try
		{
			ICommand builder = EclipseUtil.getBuilder( project, Builder.ID );
			if( builder == null )
				return parent;

			Map<String, String> arguments = builder.getArguments();
			Composite top = EclipseUtil.createGrid( parent, 2, 1, true, false );

			scriptFile = EclipseUtil.createText( top, Text.PropertiesScriptFile, Builder.SCRIPT_FILE_ARGUMENT, arguments );
			scriptEngine = EclipseUtil.createText( top, Text.PropertiesScriptEngine, Builder.SCRIPT_ENGINE_ARGUMENT, arguments );
			configurationFile = EclipseUtil.createText( top, Text.PropertiesConfigurationFile, Builder.CONFIGURATION_FILE_ARGUMENT, arguments );
			defaultDir = EclipseUtil.createText( top, Text.PropertiesDefaultDir, Builder.DEFAULT_DIR_ARGUMENT, arguments );
			libraryDir = EclipseUtil.createText( top, Text.PropertiesLibraryDir, Builder.LIBRARY_DIR_ARGUMENT, arguments );
			apiDir = EclipseUtil.createText( top, Text.PropertiesApiDir, Builder.API_DIR_ARGUMENT, arguments );
			sourceDir = EclipseUtil.createText( top, Text.PropertiesSourceDir, Builder.SOURCE_DIR_ARGUMENT, arguments );
			stateFile = EclipseUtil.createText( top, Text.PropertiesStateFile, Builder.STATE_FILE_ARGUMENT, arguments );

			return top;
		}
		catch( CoreException x )
		{
			throw new RuntimeException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private org.eclipse.swt.widgets.Text scriptFile;

	private org.eclipse.swt.widgets.Text scriptEngine;

	private org.eclipse.swt.widgets.Text configurationFile;

	private org.eclipse.swt.widgets.Text defaultDir;

	private org.eclipse.swt.widgets.Text libraryDir;

	private org.eclipse.swt.widgets.Text apiDir;

	private org.eclipse.swt.widgets.Text sourceDir;

	private org.eclipse.swt.widgets.Text stateFile;

	private IProject getProject()
	{
		IAdaptable adaptable = getElement();
		return adaptable != null ? adaptable.getAdapter( IProject.class ) : null;
	}
}