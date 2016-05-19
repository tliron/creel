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
 * An event handler that outputs events to a {@link PrintWriter}. Optionally
 * supports rich ANSI output with colors and progress bar animations for ongoing
 * events.
 * 
 * @author Tal Liron
 */
public class ConsoleEventHandler extends OngoingEventHandler
{
	//
	// Construction
	//

	/**
	 * Constructor: standard out, no ANSI, no stacktrace.
	 */
	public ConsoleEventHandler()
	{
		this( false, false );
	}

	/**
	 * Constructor: standard out.
	 * 
	 * @param ansi
	 *        Whether to output ANSI colors and animations
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public ConsoleEventHandler( boolean ansi, boolean stacktrace )
	{
		this( System.out, ansi, stacktrace );
	}

	/**
	 * Constructor.
	 * 
	 * @param out
	 *        The output
	 * @param ansi
	 *        Whether to output ANSI colors and animations
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public ConsoleEventHandler( OutputStream out, boolean ansi, boolean stacktrace )
	{
		this( new PrintWriter( out ), ansi, stacktrace );
	}

	/**
	 * Constructor.
	 * 
	 * @param out
	 *        The output
	 * @param ansi
	 *        Whether to output ANSI colors and animations
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public ConsoleEventHandler( PrintWriter out, boolean ansi, boolean stacktrace )
	{
		this.out = out;
		this.ansi = ansi;
		this.stacktrace = stacktrace;
	}

	//
	// Attributes
	//

	/**
	 * Whether to output ANSI colors and animations.
	 * 
	 * @return True if ANSI is supported
	 */
	public boolean isAnsi()
	{
		return ansi;
	}

	/**
	 * Whether to print stack traces of exceptions.
	 * 
	 * @return True if printing stack traces
	 */
	public boolean isStacktrace()
	{
		return stacktrace;
	}

	/**
	 * The output.
	 * 
	 * @return The output
	 */
	public PrintWriter getOut()
	{
		return out;
	}

	/**
	 * The ANSI end event graphics code.
	 * 
	 * @return The ANSI end event graphics code
	 */
	public String getEndGraphics()
	{
		return endGraphics;
	}

	/**
	 * The ANSI end event graphics code.
	 * 
	 * @param endGraphics
	 *        The ANSI end event graphics code
	 */
	public void setEndGraphics( String endGraphics )
	{
		this.endGraphics = endGraphics;
	}

	/**
	 * The ANSI fail event graphics code.
	 * 
	 * @return The ANSI fail event graphics code
	 */
	public String getFailGraphics()
	{
		return failGraphics;
	}

	/**
	 * The ANSI fail event graphics code.
	 * 
	 * @param failGraphics
	 *        The ANSI fail event graphics code
	 */
	public void setFailGraphics( String failGraphics )
	{
		this.failGraphics = failGraphics;
	}

	/**
	 * The ANSI error event graphics code.
	 * 
	 * @return The ANSI error event graphics code
	 */
	public String getErrorGraphics()
	{
		return errorGraphics;
	}

	/**
	 * The ANSI error event graphics code.
	 * 
	 * @param errorGraphics
	 *        The ANSI error event graphics code
	 */
	public void setErrorGraphics( String errorGraphics )
	{
		this.errorGraphics = errorGraphics;
	}

	/**
	 * The ANSI ongoing event graphics code.
	 * 
	 * @return The ANSI ongoing event graphics code
	 */
	public String getOngoingGraphics()
	{
		return ongoingGraphics;
	}

	/**
	 * The ANSI ongoing event graphics code.
	 * 
	 * @param ongoingGraphics
	 *        The ANSI ongoing event graphics code
	 */
	public void setOngoingGraphics( String ongoingGraphics )
	{
		this.ongoingGraphics = ongoingGraphics;
	}

	/**
	 * The ANSI default event graphics code.
	 * 
	 * @return The ANSI default event graphics code
	 */
	public String getDefaultGraphics()
	{
		return defaultGraphics;
	}

	/**
	 * The ANSI default event graphics code.
	 * 
	 * @param defaultGraphics
	 *        The ANSI default event graphics code
	 */
	public void setDefaultGraphics( String defaultGraphics )
	{
		this.defaultGraphics = defaultGraphics;
	}

	/**
	 * Th elength of the ongoing event progress animation bar.
	 * 
	 * @return The progress length
	 */
	public int getProgressLength()
	{
		return progressLength;
	}

	/**
	 * The length of the ongoing event progress animation bar.
	 * 
	 * @param progressLength
	 *        The progress length
	 */
	public void setProgressLength( int progressLength )
	{
		this.progressLength = progressLength;
	}

	/**
	 * The ongoing event progress animation bar prefix.
	 * 
	 * @return The progress start
	 */
	public String getProgressStart()
	{
		return progressStart;
	}

	/**
	 * The ongoing event progress animation bar prefix.
	 * 
	 * @param progressStart
	 *        The progress start
	 */
	public void setProgressStart( String progressStart )
	{
		this.progressStart = progressStart;
	}

	/**
	 * The ongoing event progress animation bar suffix.
	 * 
	 * @return The progress end
	 */
	public String getProgressEnd()
	{
		return progressEnd;
	}

	/**
	 * The ongoing event progress animation bar suffix.
	 * 
	 * @param progressEnd
	 *        The progress end
	 */
	public void setProgressEnd( String progressEnd )
	{
		this.progressEnd = progressEnd;
	}

	/**
	 * The ongoing event progress animation bar done block.
	 * 
	 * @return The progress done block
	 */
	public String getProgressDone()
	{
		return progressDone;
	}

	/**
	 * The ongoing event progress animation bar done block.
	 * 
	 * @param progressDone
	 *        The progress done block
	 */
	public void setProgressDone( String progressDone )
	{
		this.progressDone = progressDone;
	}

	/**
	 * The ongoing event progress animation bar not-done block.
	 * 
	 * @return The progress not-done block
	 */
	public String getProgressTodo()
	{
		return progressTodo;
	}

	/**
	 * The ongoing event progress animation bar not-done block.
	 * 
	 * @param progressTodo
	 *        The progress not-done block
	 */
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
			ansi( Integer.toString( ongoingEventsHeight ) + 'A' );

		super.handleEvent( event );

		Event.Type type = event.getType();
		if( type == Event.Type.END )
			ansi( getEndGraphics() + 'm' );
		else if( type == Event.Type.FAIL )
			ansi( getFailGraphics() + 'm' );
		else if( type == Event.Type.ERROR )
			ansi( getErrorGraphics() + 'm' );
		else if( type == Event.Type.INFO )
			ansi( getDefaultGraphics() + 'm' );

		if( ( type != Event.Type.BEGIN ) && ( type != Event.Type.UPDATE ) && ( type != Event.Type.DEBUG ) )
			print( event );

		// Print ongoing events block after everything else
		if( isAnsi() )
			for( Event ongoingEvent : ongoingEvents )
			{
				ansi( getOngoingGraphics() + 'm' );
				print( ongoingEvent );
			}

		// Erase to end of screen
		ansi( "0J" );

		getOut().flush();

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Gets the terminal's width in characters. Defaults to
	 * {@link Integer#MAX_VALUE}. Override this to support your terminal
	 * implementation.
	 * 
	 * @return The terminal's width
	 */
	protected int getTerminalWidth()
	{
		return Integer.MAX_VALUE;
	}

	/**
	 * Prints an event, making sure to fit it in
	 * {@link ConsoleEventHandler#getTerminalWidth()}.
	 * 
	 * @param event
	 *        The event
	 */
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

		CharSequence message = event.getMessage();
		Throwable exception = event.getException();
		boolean printStackTrace = isStacktrace() && ( exception != null );
		if( ( message == null ) && ( exception != null ) && !printStackTrace )
			message = exception.toString();

		if( message != null )
		{
			output.append( message );

			// We are making sure that we always advance one row only, even if
			// we print a line longer than a row
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
				ansi( "0m", "K" );
				getOut().println();
			}
		}

		// Exception stack trace
		if( printStackTrace )
		{
			ansi( getErrorGraphics() + 'm' );
			exception.printStackTrace( getOut() );
			ansi( "0m" );
		}
	}

	/**
	 * Outputs ANSI control sequences.
	 * 
	 * @param args
	 *        The sequences
	 */
	protected void ansi( CharSequence... args )
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
