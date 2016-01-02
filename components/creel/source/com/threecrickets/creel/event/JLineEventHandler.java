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

import jline.Terminal;
import jline.TerminalFactory;

/**
 * @author Tal Liron
 */
public class JLineEventHandler extends ConsoleEventHandler
{
	//
	// Construction
	//

	public JLineEventHandler( boolean stacktrace )
	{
		this( TerminalFactory.get(), stacktrace );
	}

	public JLineEventHandler( Terminal terminal, boolean stacktrace )
	{
		super( terminal.isAnsiSupported(), stacktrace );
		this.terminal = terminal;
	}

	public JLineEventHandler( boolean ansi, boolean stacktrace )
	{
		this( TerminalFactory.get(), ansi, stacktrace );
	}

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
