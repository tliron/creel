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

import jline.Terminal;
import jline.TerminalFactory;

/**
 * A console event handler that uses
 * <a href="https://github.com/jline/jline2">JLine</a> to automatically detect
 * ANSI support and query the terminal width.
 * 
 * @author Tal Liron
 */
public class JLineEventHandler extends ConsoleEventHandler
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public JLineEventHandler( boolean stacktrace )
	{
		this( TerminalFactory.get(), stacktrace );
	}

	/**
	 * Constructor.
	 * 
	 * @param ansi
	 *        Whether to output ANSI colors and animations
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public JLineEventHandler( boolean ansi, boolean stacktrace )
	{
		this( TerminalFactory.get(), ansi, stacktrace );
	}

	/**
	 * Constructor.
	 * 
	 * @param terminal
	 *        The terminal
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public JLineEventHandler( Terminal terminal, boolean stacktrace )
	{
		super( terminal.isAnsiSupported(), stacktrace );
		this.terminal = terminal;
	}

	/**
	 * Constructor.
	 * 
	 * @param terminal
	 *        The terminal
	 * @param ansi
	 *        Whether to output ANSI colors and animations
	 * @param stacktrace
	 *        Whether to print stack traces of exceptions
	 */
	public JLineEventHandler( Terminal terminal, boolean ansi, boolean stacktrace )
	{
		super( ansi, stacktrace );
		this.terminal = terminal;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected int getTerminalWidth()
	{
		return terminal.getWidth();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	private final Terminal terminal;
}
