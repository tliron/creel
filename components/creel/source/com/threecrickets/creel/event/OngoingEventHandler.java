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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for event handlers that maintain a list of ongoing events. Will
 * intercept {@link Event.Type#BEGIN}, {@link Event.Type#END},
 * {@link Event.Type#FAIL}, and {@link Event.Type#UPDATE} events and update the
 * ongoing event list accordingly.
 * 
 * @author Tal Liron
 */
public abstract class OngoingEventHandler implements EventHandler
{
	//
	// Attributes
	//

	/**
	 * The ongoing events.
	 * 
	 * @return The ongoing events
	 */
	public Iterable<Event> getOngoingEvents()
	{
		return Collections.unmodifiableCollection( ongoingEvents );
	}

	//
	// EventHandler
	//

	public synchronized boolean handleEvent( Event event )
	{
		Event.Type type = event.getType();
		if( type == Event.Type.BEGIN )
		{
			// Add ongoing event
			if( event.getId() != null )
				ongoingEvents.add( event );
		}
		else if( ( type == Event.Type.END ) || ( type == Event.Type.FAIL ) )
		{
			// Remove ongoing event
			String id = event.getId();
			for( Event ongoingEvent : ongoingEvents )
			{
				if( ongoingEvent.getId().equals( id ) )
				{
					ongoingEvents.remove( ongoingEvent );
					break;
				}
			}
		}
		else if( type == Event.Type.UPDATE )
		{
			// Update ongoing event
			String id = event.getId();
			for( Event ongoingEvent : ongoingEvents )
			{
				if( ongoingEvent.getId().equals( id ) )
				{
					ongoingEvent.update( event );
					break;
				}
			}
		}

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final Collection<Event> ongoingEvents = new CopyOnWriteArrayList<Event>();
}
