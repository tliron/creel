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

	/**
	 * The plugin singleton or null if not started.
	 */
	public static volatile Plugin instance;

	//
	// Static attributes
	//

	/**
	 * Log helper.
	 * 
	 * @return The log helper
	 */
	public static LogHelper getLogHelper()
	{
		if( logHelper == null )
			logHelper = new LogHelper( ID );
		return logHelper;
	}

	//
	// AbstractUIPlugin
	//

	@Override
	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		instance = this;
		getLogHelper().log( IStatus.INFO, "Creel plugin started" );
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		instance = null;
		super.stop( context );
		getLogHelper().log( IStatus.INFO, "Creel plugin stopped" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static volatile LogHelper logHelper;
}
