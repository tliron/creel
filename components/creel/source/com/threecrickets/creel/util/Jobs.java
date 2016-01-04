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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

import com.threecrickets.creel.util.internal.Job;

/**
 * Powerful utility to avoid repeating concurrent identical jobs.
 * 
 * @author Tal Liron
 */
public class Jobs
{
	//
	// Operations
	//

	/**
	 * Checks if we are already doing the job. If we are not, then mark that we
	 * have begun it. If we are, then hook a task to run when that job finishes.
	 * 
	 * @param id
	 *        The job ID
	 * @param executor
	 *        The executor
	 * @param phaser
	 *        The phaser or null
	 * @param onEnd
	 *        The task to submit or run when the job finishes
	 * @return True if not already doing the job
	 */
	public synchronized boolean beginIfNotBegun( int id, ExecutorService executor, Phaser phaser, Runnable onEnd )
	{
		Job job = jobs.get( id );
		if( job == null )
		{
			// New job
			jobs.put( id, new Job( executor ) );
			return true;
		}
		else
		{
			// Another thread has already started this job, so let's wait until
			// they're done
			if( phaser != null )
				phaser.register();
			job.onEnd( onEnd );
			return false;
		}
	}

	/**
	 * Mark the job finished, which may trigger tasks to be submitted.
	 * 
	 * @param id
	 *        The job ID
	 * @return True if the job existed
	 */
	public synchronized boolean notifyEnd( int id )
	{
		Job job = jobs.remove( id );
		if( job != null )
		{
			job.end();
			return true;
		}
		else
			return false;
	}

	/**
	 * Submits the task to run when the job finishes. If already finished, runs
	 * the task now.
	 * 
	 * @param id
	 *        The job ID
	 * @param onEnd
	 *        The task
	 */
	public synchronized void onEnd( int id, Runnable onEnd )
	{
		Job job = jobs.get( id );
		if( job != null )
			// Do later
			job.onEnd( onEnd );
		else
			// Do now
			onEnd.run();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Map<Integer, Job> jobs = new HashMap<Integer, Job>();
}
