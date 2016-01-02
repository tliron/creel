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

package com.threecrickets.creel.downloader.internal;

import java.util.concurrent.Phaser;

/**
 * @author Tal Liron
 */
public class WrappedTask implements Runnable
{
	//
	// Construction
	//

	public WrappedTask( Runnable runnable, Phaser phaser )
	{
		this.runnable = runnable;
		this.phaser = phaser;
	}

	//
	// Attributes
	//

	public Runnable getRunnable()
	{
		return runnable;
	}

	public Phaser getPhaser()
	{
		return phaser;
	}

	//
	// Runnable
	//

	public void run()
	{
		getRunnable().run();
		getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Runnable runnable;

	private final Phaser phaser;
}
