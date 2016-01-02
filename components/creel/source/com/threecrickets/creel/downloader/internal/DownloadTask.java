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

		if( chunksStreamSize != -1 )
		{
			// We support chunks, so split into tasks
			AtomicInteger counter = new AtomicInteger( chunksPerFile );
			int chunkSize = chunksStreamSize / chunksPerFile;
			for( int chunk = 0; chunk < chunksPerFile; chunk++ )
			{
				int start = chunk * chunkSize;
				int length = chunk < chunksPerFile - 1 ? chunkSize : chunksStreamSize - start;
				getDownloader().getPhaser().register();
				getExecutor().submit( new DownloadChunkTask( getDownloader(), getExecutor(), getValidator(), getSourceUrl(), getFile(), start, length, chunk + 1, chunksPerFile, counter ) );
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
