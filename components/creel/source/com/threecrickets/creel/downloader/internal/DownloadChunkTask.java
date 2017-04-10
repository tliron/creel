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
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * Downloader task for downloading a chunk.
 * 
 * @author Tal Liron
 */
public class DownloadChunkTask extends DownloadTask
{
	//
	// Construction
	//

	/**
	 * Constructor
	 * 
	 * @param sourceUrl
	 *        The source URL
	 * @param file
	 *        The destination file
	 * @param start
	 *        The start of the chunk in bytes
	 * @param length
	 *        The length of the chunk in bytes
	 * @param chunk
	 *        The chunk number
	 * @param chunks
	 *        The total number of chunks
	 * @param counter
	 *        Counter of finished chunks
	 * @param downloader
	 *        The downloader
	 * @param executor
	 *        The executor
	 * @param validator
	 *        The validator or null
	 */
	public DownloadChunkTask( URL sourceUrl, File file, int start, int length, int chunk, int chunks, AtomicInteger counter, Downloader downloader, ExecutorService executor, Runnable validator )
	{
		super( sourceUrl, file, downloader, executor, validator );
		this.start = start;
		this.length = length;
		this.chunk = chunk;
		this.chunks = chunks;
		this.counter = counter;
	}

	//
	// Attributes
	//

	/**
	 * The start of the chunk in bytes
	 * 
	 * @return The start of the chunk
	 */
	public int getStart()
	{
		return start;
	}

	/**
	 * The length of the chunk in bytes
	 * 
	 * @return The lenght of the chunk
	 */
	public int getLength()
	{
		return length;
	}

	/**
	 * The chunk number.
	 * 
	 * @return The chunk number
	 */
	public int getChunk()
	{
		return chunk;
	}

	/**
	 * The total number of chunks.
	 * 
	 * @return The total number of chunks
	 */
	public int getChunks()
	{
		return chunks;
	}

	/**
	 * Counter of finished chunks.
	 * 
	 * @return The counter
	 */
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
