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

/**
 * Represents an announcement.
 * 
 * @author Tal Liron
 */
public class Event
{
	//
	// Constants
	//

	/**
	 * The event type.
	 */
	public enum Type
	{
		/**
		 * Announce that something has happened.
		 */
		INFO,
		/**
		 * Announce that an error has occurred.
		 */
		ERROR,
		/**
		 * Announcement to help debugging.
		 */
		DEBUG,
		/**
		 * Announce the start of an ongoing event.
		 */
		BEGIN,
		/**
		 * Announce the successful end of an ongoing event.
		 */
		END,
		/**
		 * Announce the failed end of an ongoing event.
		 */
		FAIL,
		/**
		 * Announce a change to an ongoing event.
		 */
		UPDATE
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param type
	 *        The type
	 * @param id
	 *        The ID of an ongoing event or null
	 * @param message
	 *        The message or null
	 * @param progress
	 *        The progress (0.0 to 1.0) or null
	 * @param exception
	 *        The exception or null
	 */
	public Event( Type type, String id, CharSequence message, Double progress, Throwable exception )
	{
		this.type = type;
		this.id = id;
		this.message = message;
		this.progress = progress;
		this.exception = exception;
	}

	//
	// Attributes
	//

	/**
	 * The type.
	 * 
	 * @return The type
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * The ID of an ongoing event or null
	 * 
	 * @return The ID or null
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * The message or null.
	 * 
	 * @return The message or null
	 */
	public CharSequence getMessage()
	{
		return message;
	}

	/**
	 * The progress (0.0 to 1.0) or null.
	 * 
	 * @return The progress (0.0 to 1.0) or null
	 */
	public Double getProgress()
	{
		return progress;
	}

	/**
	 * The exception or null.
	 * 
	 * @return The exception or null
	 */
	public Throwable getException()
	{
		return exception;
	}

	/**
	 * Update an ongoing event by copying the message and progress from an
	 * update announcement.
	 * 
	 * @param event
	 *        The update announcement
	 */
	public void update( Event event )
	{
		if( event.message != null )
			message = event.message;
		if( event.progress != null )
			progress = event.progress;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Type type;

	private final String id;

	private CharSequence message;

	private Double progress;

	private final Throwable exception;
}
