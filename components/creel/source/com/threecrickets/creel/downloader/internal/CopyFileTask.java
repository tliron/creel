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
import java.util.concurrent.ExecutorService;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * Downloader task for copying files.
 * 
 * @author Tal Liron
 */
public class CopyFileTask extends Task
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param sourceFile
	 *        The source file
	 * @param file
	 *        The destination file
	 * @param downloader
	 *        The downloader
	 * @param executor
	 *        The executor
	 * @param validator
	 *        The validator or null
	 */
	public CopyFileTask( File sourceFile, File file, Downloader downloader, ExecutorService executor, Runnable validator )
	{
		super( file, downloader, executor, validator );
		this.sourceFile = sourceFile;
	}

	//
	// Attributes
	//

	/**
	 * The source file.
	 * 
	 * @return The source file
	 */
	public File getSourceFile()
	{
		return sourceFile;
	}

	//
	// Runnable
	//

	public void run()
	{
		if( !getSourceFile().exists() )
		{
			done( false );
			return;
		}

		String id = getDownloader().getNotifier().begin( "Copying file from " + getSourceFile() );

		try
		{
			IoUtil.copy( getSourceFile(), getFile() );
			getDownloader().getNotifier().end( id, "Copied file to " + getFile() );
			done( true );
		}
		catch( IOException x )
		{
			getDownloader().addException( x );
			getDownloader().getNotifier().fail( id, "Could not copy file from " + getSourceFile(), x );
			done( false );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File sourceFile;
}
