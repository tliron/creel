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

package com.threecrickets.creel.util;

/**
 * I/O operation progress listener.
 * 
 * @author Tal Liron
 */
public interface ProgressListener
{
	/**
	 * Called when there is progress.
	 * 
	 * @param position
	 *        The current position in the stream
	 * @param length
	 *        The total length of the stream
	 */
	public void onProgress( int position, int length );
}