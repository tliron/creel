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
import java.util.concurrent.ExecutorService;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * @author Tal Liron
 */
public class CopyFileTask extends Task
{
	//
	// Construction
	//

	public CopyFileTask( Downloader downloader, ExecutorService executor, Runnable validator, File sourceFile, File file )
	{
		super( downloader, executor, validator );
		this.sourceFile = sourceFile;
		this.file = file;
	}

	//
	// Attributes
	//

	public File getSourceFile()
	{
		return sourceFile;
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
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().fail( id, "Could not copy file from " + getSourceFile(), x );
		}
		done( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File sourceFile;

	private final File file;
}
