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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;

/**
 * Base class for downloader tasks.
 * 
 * @author Tal Liron
 */
public abstract class Task implements Runnable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param file
	 *        The destination file
	 * @param downloader
	 *        The downloader
	 * @param executor
	 *        The executor
	 * @param validator
	 *        The validator or null
	 */
	public Task( File file, Downloader downloader, ExecutorService executor, Runnable validator )
	{
		this.file = file;
		this.downloader = downloader;
		this.executor = executor;
		this.validator = validator;
	}

	//
	// Attributes
	//

	/**
	 * The destination file.
	 * 
	 * @return The destination file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * The downloader.
	 * 
	 * @return The downloader
	 */
	public Downloader getDownloader()
	{
		return downloader;
	}

	/**
	 * The executor.
	 * 
	 * @return The executor
	 */
	public ExecutorService getExecutor()
	{
		return executor;
	}

	/**
	 * The validator or null.
	 * 
	 * @return The validator or null
	 */
	public Runnable getValidator()
	{
		return validator;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Announce the task done.
	 * <p>
	 * If the file was successfully downloaded, will run the validator and
	 * increment the download count.
	 * 
	 * @param downloaded
	 *        True if downloaded a file
	 */
	protected void done( boolean downloaded )
	{
		if( downloaded )
		{
			if( getValidator() != null )
				getDownloader().submit( getValidator() );
			getDownloader().incrementCount();
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	/**
	 * Announce the partial task done. Decrements the counter, and when it
	 * reaches 0 calls {@link Task#done(boolean)}.
	 * 
	 * @param counter
	 */
	protected void done( AtomicInteger counter )
	{
		if( counter.decrementAndGet() == 0 )
			done( true );
		else
			getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final Downloader downloader;

	private final ExecutorService executor;

	private final Runnable validator;
}
