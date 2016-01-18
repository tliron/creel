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

package com.threecrickets.creel.util.internal;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import com.threecrickets.creel.util.Jobs;

/**
 * See {@link Jobs}.
 * 
 * @author Tal Liron
 */
public class Job
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param executor
	 *        The executor
	 */
	public Job( ExecutorService executor )
	{
		this.executor = executor;
	}

	//
	// Operations
	//

	/**
	 * Adds a task to run when the job finishes.
	 * 
	 * @param onEnd
	 *        The task
	 */
	public void onEnd( Runnable onEnd )
	{
		this.onEnd.add( onEnd );
	}

	/**
	 * Mark the job finished. If there are tasks to run, submit them to the
	 * executor.
	 */
	public void end()
	{
		for( Runnable onEnd : this.onEnd )
			executor.submit( onEnd );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ExecutorService executor;

	private final Collection<Runnable> onEnd = new CopyOnWriteArrayList<Runnable>();
}
