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

package com.threecrickets.creel.event;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An event handler that outputs events to a standard JVM logger.
 * 
 * @author Tal Liron
 */
public class LoggerEventHandler implements EventHandler
{
	//
	// Construction
	//

	/**
	 * Constructor using the global logger.
	 */
	public LoggerEventHandler()
	{
		this( Logger.getGlobal() );
	}

	/**
	 * Constructor.
	 * 
	 * @param logger
	 *        The logger
	 */
	public LoggerEventHandler( Logger logger )
	{
		this.logger = logger;
	}

	//
	// Attributes
	//

	/**
	 * The logger.
	 * 
	 * @return The logger
	 */
	public Logger getLogger()
	{
		return logger;
	}

	//
	// EventHandler
	//

	public boolean handleEvent( Event event )
	{
		Event.Type type = event.getType();
		if( type == Event.Type.UPDATE )
			return false;

		String message = null;
		if( event.getMessage() != null )
			message = event.getMessage().toString();
		else if( event.getException() != null )
			message = event.getException().getMessage();

		if( message != null )
		{
			Level level = Level.INFO;
			if( ( type == Event.Type.FAIL ) || ( type == Event.Type.ERROR ) )
				level = Level.SEVERE;
			else if( type == Event.Type.DEBUG )
				level = Level.FINE;
			logger.log( level, message, event.getException() );
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Logger logger;
}
