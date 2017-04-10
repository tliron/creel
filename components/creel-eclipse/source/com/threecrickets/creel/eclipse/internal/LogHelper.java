/**
 * Copyright 2015-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.eclipse.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Helper for {@link ILog}.
 * 
 * @author Tal Liron
 */
public class LogHelper
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param log
	 *        The log
	 * @param id
	 *        The ID
	 */
	public LogHelper( ILog log, String id )
	{
		this.log = log;
		this.id = id;
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        The ID
	 */
	public LogHelper( String id )
	{
		this( Platform.getLog( Platform.getBundle( id ) ), id );
	}

	//
	// Operations
	//

	/**
	 * Logs an {@link IStatus#INFO} severity message.
	 * 
	 * @param message
	 *        The message
	 */
	public void info( String message )
	{
		log( IStatus.INFO, message );
	}

	/**
	 * Logs an {@link IStatus#ERROR} severity exception.
	 * 
	 * @param x
	 *        The exception
	 */
	public void error( Throwable x )
	{
		log( IStatus.ERROR, x );
	}

	/**
	 * Logs a message.
	 * 
	 * @param severity
	 *        The severity level
	 * @param message
	 *        The message
	 */
	public void log( int severity, String message )
	{
		log.log( new Status( severity, id, IStatus.OK, message, null ) );
	}

	/**
	 * Logs an exception.
	 * 
	 * @param severity
	 *        The severity level
	 * @param x
	 *        The exception
	 */
	public void log( int severity, Throwable x )
	{
		log.log( new Status( severity, id, IStatus.OK, x.getMessage(), x ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ILog log;

	private final String id;
}
