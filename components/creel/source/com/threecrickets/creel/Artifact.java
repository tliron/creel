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

package com.threecrickets.creel;

import java.io.File;
import java.net.URL;

/**
 * @author Tal Liron
 */
public class Artifact
{
	//
	// Construction
	//

	public Artifact( URL sourceUrl, File file )
	{
		super();
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL sourceUrl;

	private final File file;
}
