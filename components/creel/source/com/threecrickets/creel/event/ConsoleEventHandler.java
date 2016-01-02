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

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * @author Tal Liron
 */
public class ConsoleEventHandler extends OngoingEventHandler
{
	//
	// Construction
	//

	public ConsoleEventHandler()
	{
		this( false, false );
	}

	public ConsoleEventHandler( boolean ansi, boolean stacktrace )
	{
		this( System.out, ansi, stacktrace );
	}

	public ConsoleEventHandler( OutputStream out, boolean ansi, boolean stacktrace )
	{
		this( new PrintWriter( out ), ansi, stacktrace );
	}

	public ConsoleEventHandler( PrintWriter out, boolean ansi, boolean stacktrace )
	{
		this.out = out;
		this.ansi = ansi;
		this.stacktrace = stacktrace;
	}

	//
	// Attributes
	//

	public boolean isAnsi()
	{
		return ansi;
	}

	public boolean isStacktrace()
	{
		return stacktrace;
	}

	public PrintWriter getOut()
	{
		return out;
	}

	public String getEndGraphics()
	{
		return endGraphics;
	}

	public void setEndGraphics( String endGraphics )
	{
		this.endGraphics = endGraphics;
	}

	public String getFailGraphics()
	{
		return failGraphics;
	}

	public void setFailGraphics( String failGraphics )
	{
		this.failGraphics = failGraphics;
	}

	public String getErrorGraphics()
	{
		return errorGraphics;
	}

	public void setErrorGraphics( String errorGraphics )
	{
		this.errorGraphics = errorGraphics;
	}

	public String getOngoingGraphics()
	{
		return ongoingGraphics;
	}

	public void setOngoingGraphics( String ongoingGraphics )
	{
		this.ongoingGraphics = ongoingGraphics;
	}

	public String getDefaultGraphics()
	{
		return defaultGraphics;
	}

	public void setDefaultGraphics( String defaultGraphics )
	{
		this.defaultGraphics = defaultGraphics;
	}

	public int getProgressLength()
	{
		return progressLength;
	}

	public void setProgressLength( int progressLength )
	{
		this.progressLength = progressLength;
	}

	public String getProgressStart()
	{
		return progressStart;
	}

	public void setProgressStart( String progressStart )
	{
		this.progressStart = progressStart;
	}

	public String getProgressEnd()
	{
		return progressEnd;
	}

	public void setProgressEnd( String progressEnd )
	{
		this.progressEnd = progressEnd;
	}

	public String getProgressDone()
	{
		return progressDone;
	}

	public void setProgressDone( String progressDone )
	{
		this.progressDone = progressDone;
	}

	public String getProgressTodo()
	{
		return progressTodo;
	}

	public void setProgressTodo( String progressTodo )
	{
		this.progressTodo = progressTodo;
	}

	//
	// EventHandler
	//

	public synchronized boolean handleEvent( Event event )
	{
		// Move up before the ongoing block we printed last time
		int ongoingEventsHeight = ongoingEvents.size();
		if( ongoingEventsHeight > 0 )
			controlSequence( Integer.toString( ongoingEventsHeight ) + 'A' );

		super.handleEvent( event );

		Event.Type type = event.getType();
		if( type == Event.Type.END )
			controlSequence( getEndGraphics() + 'm' );
		else if( type == Event.Type.FAIL )
			controlSequence( getFailGraphics() + 'm' );
		else if( type == Event.Type.ERROR )
			controlSequence( getErrorGraphics() + 'm' );
		else if( type == Event.Type.INFO )
			controlSequence( getDefaultGraphics() + 'm' );

		if( ( type != Event.Type.BEGIN ) && ( type != Event.Type.UPDATE ) && ( type != Event.Type.DEBUG ) )
			print( event );

		// Print ongoing events block after everything else
		if( isAnsi() )
			for( Event ongoingEvent : ongoingEvents )
			{
				controlSequence( getOngoingGraphics() + 'm' );
				print( ongoingEvent );
			}

		// Erase to end of screen
		controlSequence( "0J" );

		getOut().flush();

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected int getTerminalWidth()
	{
		return Integer.MAX_VALUE;
	}

	protected void print( Event event )
	{
		StringBuilder output = new StringBuilder();

		Double progress = event.getProgress();
		if( progress != null )
		{
			output.append( getProgressStart() );
			for( int i = 0; i < getProgressLength(); i++ )
				output.append( ( ( progress * getProgressLength() ) > i ) ? getProgressDone() : getProgressTodo() );
			// TODO: spinner at end
			output.append( getProgressEnd() );
		}

		CharSequence message = null;
		if( event.getMessage() != null )
			message = event.getMessage();
		else if( event.getException() != null )
			message = event.getException().getMessage();
		if( message != null )
			output.append( message );

		// We are making sure that we always advance one row only, even if we
		// print a line longer than a row
		int length = output.length();
		int terminalWidth = getTerminalWidth();
		if( length >= terminalWidth )
		{
			// Will automatically advance to the next line
			getOut().print( output.substring( 0, terminalWidth ) );
		}
		else
		{
			getOut().print( output );
			// Reset graphics and erase to end of line
			controlSequence( "0m", "K" );
			getOut().println();
		}

		// Exception stack trace
		if( isStacktrace() )
		{
			Throwable exception = event.getException();
			if( exception != null )
			{
				controlSequence( getErrorGraphics() + 'm' );
				exception.printStackTrace( getOut() );
				controlSequence( "0m" );
			}
		}
	}

	protected void controlSequence( CharSequence... args )
	{
		if( !isAnsi() )
			return;
		for( CharSequence c : args )
		{
			getOut().print( CSI );
			getOut().print( c );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * ANSI CSI (Control Sequence Introducer).
	 */
	private static final String CSI = "\033[";

	private final PrintWriter out;

	private final boolean ansi;

	private final boolean stacktrace;

	private volatile String endGraphics = "32";

	private volatile String failGraphics = "31";

	private volatile String errorGraphics = "31";

	private volatile String ongoingGraphics = "33";

	private volatile String defaultGraphics = "34";

	private volatile int progressLength = 16;

	private volatile String progressStart = "[";

	private volatile String progressEnd = "] ";

	private volatile String progressDone = "=";

	private volatile String progressTodo = " ";
}
