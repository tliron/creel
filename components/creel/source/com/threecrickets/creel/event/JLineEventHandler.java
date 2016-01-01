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

	public JLineEventHandler()
	{
		this( TerminalFactory.get() );
	}

	public JLineEventHandler( Terminal terminal )
	{
		super( terminal.isAnsiSupported() );
		this.terminal = terminal;
	}

	public JLineEventHandler( boolean ansi )
	{
		this( TerminalFactory.get(), ansi );
	}

	public JLineEventHandler( Terminal terminal, boolean ansi )
	{
		super( ansi );
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
