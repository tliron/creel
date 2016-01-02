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
public class DownloadChunkTask extends DownloadTask
{
	//
	// Construction
	//

	public DownloadChunkTask( Downloader downloader, ExecutorService executor, Runnable validator, URL sourceUrl, File file, int start, int length, int chunk, int chunks, AtomicInteger counter )
	{
		super( downloader, executor, validator, sourceUrl, file );
		this.start = start;
		this.length = length;
		this.chunk = chunk;
		this.chunks = chunks;
		this.counter = counter;
	}

	//
	// Attributes
	//

	public int getStart()
	{
		return start;
	}

	public int getLength()
	{
		return length;
	}

	public int getChunk()
	{
		return chunk;
	}

	public int getChunks()
	{
		return chunks;
	}

	public AtomicInteger getCounter()
	{
		return counter;
	}

	//
	// Runnable
	//

	public void run()
	{
		id = getDownloader().getNotifier().begin( "Downloading from " + getSourceUrl() + " (" + getChunk() + "/" + getChunks() + ")" );

		try
		{
			URLConnection connection = IoUtil.openRange( getSourceUrl(), getStart(), getLength() );
			IoUtil.copy( connection.getInputStream(), getFile(), getStart(), this, getLength() );
			getDownloader().getNotifier().end( id, "Downloaded to " + getFile() + " (" + getChunk() + "/" + getChunks() + ")" );
			done( getCounter() );
		}
		catch( IOException x )
		{
			getDownloader().addException( x );
			getDownloader().getNotifier().fail( id, "Could not download from " + getSourceUrl() + " (" + getChunk() + "/" + getChunks() + ")", x );
			done( false );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final int start;

	private final int length;

	private final int chunk;

	private final int chunks;

	private final AtomicInteger counter;

	private String id;
}
