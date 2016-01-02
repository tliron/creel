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

import com.threecrickets.creel.downloader.Downloader;

/**
 * @author Tal Liron
 */
public class WrappedTask implements Runnable
{
	//
	// Construction
	//

	public WrappedTask( Runnable runnable, Downloader downloader )
	{
		this.runnable = runnable;
		this.downloader = downloader;
	}

	//
	// Attributes
	//

	public Runnable getRunnable()
	{
		return runnable;
	}

	public Downloader getDownloader()
	{
		return downloader;
	}

	//
	// Runnable
	//

	public void run()
	{
		try
		{
			getRunnable().run();
		}
		catch( Throwable x )
		{
			getDownloader().addException( x );
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Runnable runnable;

	private final Downloader downloader;
}
