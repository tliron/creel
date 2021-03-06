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

package com.threecrickets.creel.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Tal Liron
 */
public abstract class IoUtil
{
	//
	// Static attributes
	//

	/**
	 * The timeout for operations in milliseconds. Defaults to 8000.
	 */
	public static volatile int timeout = 8000;

	/**
	 * The buffer size in bytes. Defaults to 16Kb.
	 */
	public static volatile int bufferSize = 16 * 1024;

	/**
	 * The user agent string for URL connections. Defaults to "Creel".
	 */
	public static volatile String userAgent = "Creel";

	//
	// Static operations
	//

	/**
	 * Converts a URL to a file if it has the "file:" spec.
	 * 
	 * @param url
	 *        The URL
	 * @return The file or null
	 */
	public static File toFile( URL url )
	{
		if( "file".equalsIgnoreCase( url.getProtocol() ) )
		{
			try
			{
				return new File( url.toURI() );
			}
			catch( URISyntaxException x )
			{
				// This should never happen
			}
		}
		return null;
	}

	/**
	 * Opens a URL connection with sensible defaults.
	 * 
	 * @param url
	 *        The URL
	 * @return The connection
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static URLConnection open( URL url ) throws IOException
	{
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout( timeout );
		connection.setReadTimeout( timeout );
		connection.addRequestProperty( "User-Agent", userAgent );
		return connection;
	}

	public static URLConnection openRange( URL url, int start, int length ) throws IOException
	{
		URLConnection connection = open( url );
		connection.setRequestProperty( "Range", "bytes=" + start + "-" + ( start + length ) );
		return connection;
	}

	/**
	 * True if the URL points to a reachable resource.
	 * 
	 * @param url
	 *        The URL
	 * @return True if valid
	 */
	public static boolean exists( URL url )
	{
		File file = toFile( url );
		if( file != null )
			return file.exists();

		try
		{
			URLConnection connection = open( url );
			if( connection instanceof HttpURLConnection )
			{
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod( "HEAD" );
				return httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
			}
			else
			{
				connection.getInputStream().close();
				return true;
			}
		}
		catch( IOException x )
		{
			return false;
		}
	}

	/**
	 * Checks if the URL supports ranges.
	 * 
	 * @param url
	 *        The URL
	 * @return Stream size in bytes if supports ranges, -1 otherwise
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static int supportsRanges( URL url ) throws IOException
	{
		// Make sure the host supports ranges
		URLConnection connection = open( url );
		String acceptRanges = connection.getHeaderField( "Accept-Ranges" );
		if( "bytes".equals( acceptRanges ) )
			return connection.getContentLength();
		return -1;
	}

	/**
	 * Deletes the file, including empty parent directories up to the root
	 * directory.
	 *
	 * @param file
	 *        The file
	 * @param rootDir
	 *        The root directory
	 * @return True if deleted
	 */
	public static boolean deleteWithParentDirectories( File file, File rootDir )
	{
		while( true )
		{
			if( file.isDirectory() )
			{
				if( !file.delete() )
					break;
			}
			else if( file.exists() && !file.delete() )
				return false;
			file = file.getParentFile();
			if( ( file == null ) || file.equals( rootDir ) )
				break;
		}
		return true;
	}

	// Copy

	/**
	 * Copies entire channels. Does <i>not</i> close the channels when done.
	 * 
	 * @param source
	 *        The source channel
	 * @param target
	 *        The target channel
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( ReadableByteChannel source, WritableByteChannel target, ProgressListener progressListener, int length ) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate( bufferSize );
		int count, position = 0;
		while( ( count = source.read( buffer ) ) != -1 )
		{
			buffer.flip();
			while( buffer.hasRemaining() )
				target.write( buffer );
			buffer.clear();
			position += count;
			if( progressListener != null )
				progressListener.onProgress( position, length );
		}
	}

	/**
	 * Copies entire streams. Does <i>not</i> close the streams when done.
	 * 
	 * @param source
	 *        The source input stream
	 * @param target
	 *        The target output stream
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( InputStream source, OutputStream target, ProgressListener progressListener, int length ) throws IOException
	{
		byte[] buffer = new byte[bufferSize];
		int count, position = 0;
		while( ( count = source.read( buffer ) ) != -1 )
		{
			target.write( buffer, 0, count );
			position += count;
			if( progressListener != null )
				progressListener.onProgress( position, length );
		}
	}

	/**
	 * Copies an entire stream to a file at a specific location. Does <i>not</i>
	 * close the stream when done.
	 * 
	 * @param source
	 *        The source input stream
	 * @param target
	 *        The target random-access file
	 * @param start
	 *        The start position in the file
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( InputStream source, File target, int start, ProgressListener progressListener, int length ) throws IOException
	{
		Files.createDirectories( target.toPath().getParent() );
		RandomAccessFile random = new RandomAccessFile( target, "rw" );
		try
		{
			random.seek( start );
			byte[] buffer = new byte[bufferSize];
			int count, position = 0;
			while( ( count = source.read( buffer ) ) != -1 )
			{
				random.write( buffer, 0, count );
				position += count;
				if( progressListener != null )
					progressListener.onProgress( position, length );
			}
		}
		finally
		{
			random.close();
		}
	}

	/**
	 * Copies all data from a URL to a file, creating necessary parent
	 * directories at destination.
	 * <p>
	 * Will detect "file:" URLs and optimize accordingly.
	 * 
	 * @param source
	 *        The source URL
	 * @param target
	 *        The target file
	 * @param progressListener
	 *        The progress listener or null
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( URL source, File target, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( source );
		if( file != null )
		{
			// Optimize for file copies
			copy( file, target );
			return;
		}

		URLConnection connection = open( source );
		InputStream in = connection.getInputStream();
		try
		{
			Files.createDirectories( target.toPath().getParent() );
			OutputStream out = new FileOutputStream( target );
			try
			{
				copy( in, out, progressListener, connection.getContentLength() );
			}
			finally
			{
				out.close();
			}
		}
		finally
		{
			in.close();
		}
	}

	/**
	 * Copies a file, creating necessary parent directories at destination.
	 * 
	 * @param source
	 *        The source file
	 * @param target
	 *        The target file
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( File source, File target ) throws IOException
	{
		if( !source.exists() )
			throw new FileNotFoundException( source.toString() );
		Files.createDirectories( target.toPath().getParent() );
		Files.copy( source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING );
	}

	// Read

	/**
	 * Reads all bytes from a stream. Does <i>not</i> close the stream when
	 * done. If you know the stream is of a file and you don't absolutely need
	 * an array of bytes, use {@link IoUtil#readBuffer(File)}, which is more
	 * efficient.
	 * 
	 * @param source
	 *        The source input stream
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] readBytes( InputStream source, ProgressListener progressListener, int length ) throws IOException
	{
		ReadableByteChannel fromChannel = Channels.newChannel( source );
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream( bufferSize );
			WritableByteChannel toChannel = Channels.newChannel( buffer );
			copy( fromChannel, toChannel, progressListener, length );
			return buffer.toByteArray();
		}
		finally
		{
			fromChannel.close();
		}
	}

	/**
	 * Reads all bytes from a URL. If you know the URL is of a file and you
	 * don't absolutely need an array of bytes, use
	 * {@link IoUtil#readBuffer(File)}, which is more efficient.
	 * 
	 * @param url
	 *        The URL
	 * @param progressListener
	 *        The progress listener or null
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] readBytes( URL url, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( url );
		if( file != null )
			// Use readBytes(File) if possible, because it is more efficient
			// (because we know the buffer size in advance)
			return readBytes( file );

		URLConnection connection = open( url );
		return readBytes( connection.getInputStream(), progressListener, connection.getContentLength() );
	}

	/**
	 * Reads all bytes from a file. If you don't absolutely need an array of
	 * bytes, use {@link IoUtil#readBuffer(File)}, which is more efficient.
	 * 
	 * @param file
	 *        The file
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] readBytes( File file ) throws IOException
	{
		FileInputStream input = new FileInputStream( file );
		try
		{
			FileChannel channel = input.getChannel();
			try
			{
				byte[] bytes = new byte[(int) channel.size()];
				channel.read( ByteBuffer.wrap( bytes ) );
				return bytes;
			}
			finally
			{
				channel.close();
			}
		}
		catch( FileNotFoundException x )
		{
			return null;
		}
		finally
		{
			input.close();
		}
	}

	/**
	 * Reads all bytes from a file as a memory-mapped buffer. This is more
	 * efficient than {@link IoUtil#readBytes(File)}.
	 * 
	 * @param file
	 *        The file
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static ByteBuffer readBuffer( File file ) throws IOException
	{
		FileInputStream input = new FileInputStream( file );
		try
		{
			FileChannel channel = input.getChannel();
			try
			{
				return channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
			}
			finally
			{
				channel.close();
			}
		}
		catch( FileNotFoundException x )
		{
			return null;
		}
		finally
		{
			input.close();
		}
	}

	/**
	 * Reads all UTF-8 text from a stream. Does <i>not</i> close the stream when
	 * done.
	 * 
	 * @param source
	 *        The source input stream
	 * @param progressListener
	 *        The progress listener or null
	 * @return The content
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static String readText( InputStream source, ProgressListener progressListener ) throws IOException
	{
		return new String( readBytes( source, progressListener, -1 ), StandardCharsets.UTF_8 );
	}

	/**
	 * Reads all UTF-8 text from a URL.
	 * 
	 * @param url
	 *        The URL
	 * @param progressListener
	 *        The progress listener or null
	 * @return The content
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static String readText( URL url, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( url );
		if( file != null )
			// Use readText(File) if possible, because it is more efficient
			// (because we know the buffer size in advance)
			return readText( file );

		return new String( readBytes( url, progressListener ), StandardCharsets.UTF_8 );
	}

	/**
	 * Reads all UTF-8 text from a file.
	 * 
	 * @param file
	 *        The file
	 * @return The file's content
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static String readText( File file ) throws IOException
	{
		return StandardCharsets.UTF_8.decode( readBuffer( file ) ).toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private IoUtil()
	{
	}
}
