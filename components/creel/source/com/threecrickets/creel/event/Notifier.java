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

package com.threecrickets.creel.event;

import java.util.UUID;

import com.threecrickets.creel.event.Event.Type;

/**
 * Utility class to make it easier to work with {@link EventHandler}.
 * 
 * @author Tal Liron
 */
public class Notifier
{
	//
	// Static attributes
	//

	/**
	 * The last notifier created that is not a {@link NullEventHandler}.
	 */
	public static volatile Notifier lastInstance;

	//
	// Construction
	//

	/**
	 * Constructor using a {@link NullEventHandler}.
	 */
	public Notifier()
	{
		this( NullEventHandler.INSTANCE );
	}

	/**
	 * Constructor.
	 * 
	 * @param eventHandler
	 *        The event handler or null
	 */
	public Notifier( EventHandler eventHandler )
	{
		this.eventHandler = eventHandler;
		if( !( eventHandler instanceof NullEventHandler ) )
			lastInstance = this;
	}

	//
	// Attributes
	//

	/**
	 * The event handler or null.
	 * 
	 * @return The event hander or null
	 */
	public EventHandler getEventHandler()
	{
		return eventHandler;
	}

	/**
	 * The event handler or null.
	 * 
	 * @param eventHandler
	 *        The event handler or null
	 */
	public void setEventHandler( EventHandler eventHandler )
	{
		this.eventHandler = eventHandler;
	}

	//
	// Operations
	//

	/**
	 * Creates a unique ID to be used for ongoing events.
	 * 
	 * @return The unique ID
	 */
	public String newId()
	{
		return UUID.randomUUID().toString();
	}

	/**
	 * Sends an event to the event handler if its not null.
	 * 
	 * @param type
	 *        The event type
	 * @param id
	 *        The ID of an ongoing event or null
	 * @param message
	 *        The message or null
	 * @param progress
	 *        The progress (0.0 to 1.0) or null
	 * @param exception
	 *        The exception or null
	 */
	public void fireEvent( Type type, String id, CharSequence message, Double progress, Throwable exception )
	{
		EventHandler eventHandler = this.eventHandler;
		if( eventHandler != null )
			eventHandler.handleEvent( new Event( type, id, message, progress, exception ) );
	}

	/**
	 * Sends a {@link Event.Type#INFO} event.
	 * 
	 * @param message
	 *        The message or null
	 */
	public void info( CharSequence message )
	{
		fireEvent( Event.Type.INFO, null, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#ERROR} event.
	 * 
	 * @param message
	 *        The message or null
	 */
	public void error( CharSequence message )
	{
		fireEvent( Event.Type.ERROR, null, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#ERROR} event.
	 * 
	 * @param exception
	 *        The exception or null
	 */
	public void error( Throwable exception )
	{
		fireEvent( Event.Type.ERROR, null, exception != null ? exception.getMessage() : null, null, exception );
	}

	/**
	 * Sends a {@link Event.Type#ERROR} event.
	 * 
	 * @param message
	 *        The message or null
	 * @param exception
	 *        The exception or null
	 */
	public void error( CharSequence message, Throwable exception )
	{
		fireEvent( Event.Type.ERROR, null, message, null, exception );
	}

	/**
	 * Sends a {@link Event.Type#DEBUG} event.
	 * 
	 * @param message
	 *        The message or null
	 */
	public void debug( CharSequence message )
	{
		fireEvent( Event.Type.DEBUG, null, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#BEGIN} event with a new, unique ongoing event
	 * ID and returns it.
	 * 
	 * @param message
	 *        The message or null
	 * @return The ongoing event ID
	 */
	public String begin( CharSequence message )
	{
		String id = newId();
		fireEvent( Event.Type.BEGIN, id, message, null, null );
		return id;
	}

	/**
	 * Sends a {@link Event.Type#BEGIN} event with a new, unique ongoing event
	 * ID and returns it.
	 * 
	 * @param message
	 *        The message or null
	 * @param progress
	 *        The initial progress (0.0 to 1.0)
	 * @return The ongoing event ID
	 */
	public String begin( CharSequence message, double progress )
	{
		String id = newId();
		fireEvent( Event.Type.BEGIN, id, message, progress, null );
		return id;
	}

	/**
	 * Sends a {@link Event.Type#UPDATE} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param message
	 *        The message or null
	 * @param progress
	 *        The new progress (0.0 to 1.0)
	 */
	public void update( String id, CharSequence message, double progress )
	{
		fireEvent( Event.Type.UPDATE, id, message, progress, null );
	}

	/**
	 * Sends a {@link Event.Type#UPDATE} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param message
	 *        The message or null
	 */
	public void update( String id, CharSequence message )
	{
		fireEvent( Event.Type.UPDATE, id, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#UPDATE} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param progress
	 *        The new progress (0.0 to 1.0)
	 */
	public void update( String id, double progress )
	{
		fireEvent( Event.Type.UPDATE, id, null, progress, null );
	}

	/**
	 * Sends a {@link Event.Type#END} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param message
	 *        The message or null
	 */
	public void end( String id, CharSequence message )
	{
		fireEvent( Event.Type.END, id, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#FAIL} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param message
	 *        The message or null
	 */
	public void fail( String id, CharSequence message )
	{
		fireEvent( Event.Type.FAIL, id, message, null, null );
	}

	/**
	 * Sends a {@link Event.Type#FAIL} event.
	 * 
	 * @param id
	 *        The ongoing event ID
	 * @param message
	 *        The message or null
	 * @param exception
	 *        The exception or null
	 */
	public void fail( String id, CharSequence message, Throwable exception )
	{
		fireEvent( Event.Type.FAIL, id, message, null, exception );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile EventHandler eventHandler;
}
