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
 * @author Tal Liron
 */
public class Jobs
{
	//
	// Operations
	//

	public synchronized boolean beginIfNotBegun( int token, ExecutorService executor, Phaser phaser, Runnable onEnd )
	{
		Job job = jobs.get( token );
		if( job == null )
		{
			// New job
			jobs.put( token, new Job( executor ) );
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

	public synchronized boolean notifyEnd( int token )
	{
		Job job = jobs.remove( token );
		if( job != null )
		{
			job.end();
			return true;
		}
		else
			return false;
	}

	public synchronized void onEnd( int token, Runnable onEnd )
	{
		Job job = jobs.get( token );
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
