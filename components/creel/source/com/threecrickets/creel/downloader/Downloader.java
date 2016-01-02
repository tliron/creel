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

package com.threecrickets.creel.downloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.internal.CopyFileTask;
import com.threecrickets.creel.downloader.internal.DownloadTask;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.internal.DaemonThreadFactory;
import com.threecrickets.creel.util.IoUtil;

/**
 * Fast file downloader supporting concurrent downloads and chunks. Just give it
 * a list of source URLs to download and it will do the rest. Network (HTTP,
 * FTP, etc.) URLs are supported, while file URLs are automatically optimized to
 * use fast copying.
 * 
 * @author Tal Liron
 */
public class Downloader implements Closeable
{
	//
	// Construction
	//

	public Downloader( int threadsPerHost, int chunksPerFile, Notifier notifier )
	{
		this.threadsPerHost = threadsPerHost;
		this.chunksPerFile = chunksPerFile;
		this.notifier = notifier != null ? notifier : new Notifier();
	}

	//
	// Attributes
	//

	public int getThreadsPerHost()
	{
		return threadsPerHost;
	}

	public int getChunksPerFile()
	{
		return chunksPerFile;
	}

	public ExecutorService getExecutor( String key )
	{
		ExecutorService executor = executors.get( key );
		if( executor == null )
		{
			executor = Executors.newFixedThreadPool( getThreadsPerHost(), DaemonThreadFactory.INSTANCE );
			ExecutorService existing = executors.putIfAbsent( key, executor );
			if( existing != null )
				executor = existing;
		}
		return executor;
	}

	public Iterable<Throwable> getExceptions()
	{
		return Collections.unmodifiableCollection( exceptions );
	}

	public void addException( Throwable x )
	{
		exceptions.add( x );
	}

	public Notifier getNotifier()
	{
		return notifier;
	}

	public Phaser getPhaser()
	{
		return phaser;
	}

	public int getCount()
	{
		return count.get();
	}

	public void incrementCount()
	{
		count.incrementAndGet();
	}

	public int getDelay()
	{
		return delay;
	}

	public void setDelay( int delay )
	{
		this.delay = delay;
	}

	//
	// Operations
	//

	public void submit( URL sourceUrl, File file, Runnable validator )
	{
		try
		{
			Files.createDirectories( file.toPath().getParent() );
		}
		catch( IOException x )
		{
			getNotifier().error( "Could not create file " + file, x );
			return;
		}

		ExecutorService executor = getExecutor( sourceUrl.getHost() );

		File sourceFile = IoUtil.toFile( sourceUrl );
		if( sourceFile != null )
		{
			// Optimize for file copies
			getPhaser().register();
			executor.submit( new CopyFileTask( this, executor, validator, sourceFile, file ) );
		}
		else
		{
			getPhaser().register();
			executor.submit( new DownloadTask( this, executor, validator, sourceUrl, file ) );
		}
	}

	public void waitUntilDone()
	{
		phaser.arriveAndAwaitAdvance();
	}

	//
	// Closeable
	//

	public void close()
	{
		Collection<ExecutorService> executors = new ArrayList<ExecutorService>( this.executors.values() );
		this.executors.clear();
		for( ExecutorService executor : executors )
			executor.shutdown();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final int threadsPerHost;

	private final int chunksPerFile;

	private final Notifier notifier;

	private final ConcurrentMap<String, ExecutorService> executors = new ConcurrentHashMap<String, ExecutorService>();

	private final Collection<Throwable> exceptions = new CopyOnWriteArrayList<Throwable>();

	private final Phaser phaser = new Phaser( 1 );

	private final AtomicInteger count = new AtomicInteger();

	private volatile int delay;
}
