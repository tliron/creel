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

package com.threecrickets.creel.downloader.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.ProgressListener;

/**
 * Downloader task for downloading. If the downloader wants to use chunks, and
 * the source URL supports ranges, will submit several {@link DownloadChunkTask}
 * instances to the executor. Otherwise, will download the whole file as a
 * single stream.
 * 
 * @author Tal Liron
 */
public class DownloadTask extends Task implements ProgressListener
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param sourceUrl
	 *        The source URL
	 * @param file
	 *        The destination file
	 * @param downloader
	 *        The downloader
	 * @param executor
	 *        The executor
	 * @param validator
	 *        The validator or null
	 */
	public DownloadTask( URL sourceUrl, File file, Downloader downloader, ExecutorService executor, Runnable validator )
	{
		super( file, downloader, executor, validator );
		this.sourceUrl = sourceUrl;
	}

	//
	// Attributes
	//

	/**
	 * The source URL.
	 * 
	 * @return The source URL
	 */
	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	//
	// Runnable
	//

	public void run()
	{
		if( !IoUtil.exists( getSourceUrl() ) )
		{
			done( false );
			return;
		}

		int chunksStreamSize = -1;
		int chunksPerFile = getDownloader().getChunksPerFile();

		try
		{
			if( chunksPerFile > 1 )
				chunksStreamSize = IoUtil.supportsRanges( getSourceUrl() );
		}
		catch( IOException x )
		{
			getDownloader().addException( x );
			getDownloader().getNotifier().error( "Could not access " + getSourceUrl(), x );
			done( false );
			return;
		}

		if( chunksStreamSize >= getDownloader().getMinimumSizeForChunk() )
		{
			// We support chunks, so split into tasks
			AtomicInteger counter = new AtomicInteger( chunksPerFile );
			int chunkSize = chunksStreamSize / chunksPerFile;
			for( int chunk = 0; chunk < chunksPerFile; chunk++ )
			{
				int start = chunk * chunkSize;
				int length = chunk < chunksPerFile - 1 ? chunkSize : chunksStreamSize - start;
				getDownloader().getPhaser().register();
				getExecutor().submit( new DownloadChunkTask( getSourceUrl(), getFile(), start, length, chunk + 1, chunksPerFile, counter, getDownloader(), getExecutor(), getValidator() ) );
			}
			done( false );
		}
		else
		{
			// We don't support chunks, so download now
			id = getDownloader().getNotifier().begin( "Downloading from " + getSourceUrl() );
			try
			{
				IoUtil.copy( getSourceUrl(), getFile(), this );
				getDownloader().getNotifier().end( id, "Downloaded to " + getFile() );
				done( true );
			}
			catch( IOException x )
			{
				getDownloader().addException( x );
				getDownloader().getNotifier().fail( id, "Could not download from " + getSourceUrl(), x );
				done( false );
			}
		}
	}

	//
	// ProgressListener
	//

	public void onProgress( int position, int length )
	{
		if( length > 0 )
			getDownloader().getNotifier().update( id, (double) position / (double) length );
		if( getDownloader().getDelay() > 0 )
		{
			try
			{
				Thread.sleep( getDownloader().getDelay() );
			}
			catch( InterruptedException e )
			{
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL sourceUrl;

	private String id;
}
