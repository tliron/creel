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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.threecrickets.creel.eclipse.internal.LogHelper;

/**
 * The Creel plugin. This class also functions as an OSGi bundle activator.
 * <p>
 * Plugin ID: "com.threecrickets.creel"
 * 
 * @author Tal Liron
 */
public class Plugin extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.creel";

	public static final String INTERNAL_INSTALLATION_BUNDLE = "com.threecrickets.creel";

	//
	// Static attributes
	//

	public static Plugin getDefault()
	{
		return plugin;
	}

	public static LogHelper getLogHelper()
	{
		if( logHelper == null )
			logHelper = new LogHelper( ID );
		return logHelper;
	}

	//
	// Attributes
	//

	//
	// AbstractUIPlugin
	//

	@Override
	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		plugin = this;
		getLogHelper().log( IStatus.INFO, "Creel plugin started" );
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		plugin = null;
		super.stop( context );
		getLogHelper().log( IStatus.INFO, "Creel plugin stopped" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static volatile Plugin plugin;

	private static volatile LogHelper logHelper;
}
