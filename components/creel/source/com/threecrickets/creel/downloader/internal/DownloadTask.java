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
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * @author Tal Liron
 */
public class DownloadTask extends Task implements IoUtil.ProgressListener
{
	//
	// Construction
	//

	public DownloadTask( Downloader downloader, ExecutorService executor, Runnable validator, URL sourceUrl, File file )
	{
		super( downloader, executor, validator );
		this.sourceUrl = sourceUrl;
		this.file = file;
	}

	//
	// Attributes
	//

	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	public File getFile()
	{
		return file;
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

		boolean supportsChunks = false;
		int streamSize = 0;
		int chunksPerFile = getDownloader().getChunksPerFile();

		try
		{
			if( chunksPerFile > 1 )
			{
				// Make sure the host supports ranges
				URLConnection connection = getSourceUrl().openConnection();
				String acceptRanges = connection.getHeaderField( "Accept-Ranges" );
				supportsChunks = "bytes".equals( acceptRanges );
				if( supportsChunks )
				{
					streamSize = connection.getContentLength();
					if( streamSize == -1 )
						supportsChunks = false;
				}
			}
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().error( x );
		}

		if( supportsChunks )
		{
			// We support chunks, so split into tasks
			AtomicInteger counter = new AtomicInteger( chunksPerFile );
			int chunkSize = streamSize / chunksPerFile;
			for( int chunk = 0; chunk < chunksPerFile; chunk++ )
			{
				int start = chunk * chunkSize;
				int length = chunk < chunksPerFile - 1 ? chunkSize : streamSize - start;
				getDownloader().getPhaser().register();
				getExecutor().submit( new DownloadChunkTask( getDownloader(), getExecutor(), getValidator(), getSourceUrl(), getFile(), start, length, chunk + 1, chunksPerFile, counter ) );
			}
		}
		else
		{
			// We don't support chunks, so download no
			id = getDownloader().getNotifier().begin( "Downloading from " + getSourceUrl() );
			try
			{
				IoUtil.copy( getSourceUrl(), getFile(), this );
				getDownloader().getNotifier().end( id, "Downloaded to " + getFile() );
			}
			catch( Exception x )
			{
				getDownloader().getNotifier().fail( id, "Could not download from " + getSourceUrl(), x );
			}
		}

		done( true );
	}

	//
	// IoUtil.Listener
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

	private final File file;

	private String id;
}
