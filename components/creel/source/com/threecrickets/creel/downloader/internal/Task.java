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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;

/**
 * @author Tal Liron
 */
public abstract class Task implements Runnable
{
	//
	// Construction
	//

	public Task( Downloader downloader, ExecutorService executor, Runnable validator )
	{
		this.downloader = downloader;
		this.executor = executor;
		this.validator = validator;
	}

	//
	// Attributes
	//

	public Downloader getDownloader()
	{
		return downloader;
	}

	public ExecutorService getExecutor()
	{
		return executor;
	}

	public Runnable getValidator()
	{
		return validator;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void done( boolean downloaded )
	{
		if( downloaded )
		{
			if( getValidator() != null )
			{
				getDownloader().getPhaser().register();
				getExecutor().submit( new WrappedTask( getValidator(), getDownloader() ) );
			}
			getDownloader().incrementCount();
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	protected void done( AtomicInteger counter )
	{
		if( counter.decrementAndGet() == 0 )
			done( true );
		else
			getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Downloader downloader;

	private final ExecutorService executor;

	private final Runnable validator;
}
