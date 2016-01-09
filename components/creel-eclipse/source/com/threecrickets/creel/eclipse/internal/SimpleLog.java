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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Helper for {@link ILog}.
 * 
 * @author Tal Liron
 */
public class SimpleLog
{
	//
	// Construction
	//

	public SimpleLog( ILog log, String id )
	{
		this.log = log;
		this.id = id;
	}

	public SimpleLog( String id )
	{
		this( Platform.getLog( Platform.getBundle( id ) ), id );
	}

	//
	// Operations
	//

	public void log( int severity, Throwable x )
	{
		log.log( new Status( severity, id, IStatus.OK, x.getMessage(), x ) );
	}

	public void log( int severity, String message )
	{
		log.log( new Status( severity, id, IStatus.OK, message, null ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ILog log;

	private final String id;
}
