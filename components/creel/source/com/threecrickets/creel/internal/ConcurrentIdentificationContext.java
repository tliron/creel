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

package com.threecrickets.creel.internal;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import com.threecrickets.creel.Engine;
import com.threecrickets.creel.Module;
import com.threecrickets.creel.util.Jobs;

/**
 * Used in the identification phase of the engine.
 * 
 * @author Tal Liron
 */
public class ConcurrentIdentificationContext implements Closeable
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param threads
	 *        Number of threads for the executor
	 */
	public ConcurrentIdentificationContext( int threads )
	{
		executor = Executors.newFixedThreadPool( threads, DaemonThreadFactory.INSTANCE );
	}

	//
	// Attributes
	//

	/**
	 * The executor.
	 * 
	 * @return The executor
	 */
	public ExecutorService getExecutor()
	{
		return executor;
	}

	/**
	 * The phaser.
	 * 
	 * @return The phaser
	 */
	public Phaser getPhaser()
	{
		return phaser;
	}

	/**
	 * The jobs.
	 * 
	 * @return The jobs
	 */
	public Jobs getJobs()
	{
		return jobs;
	}

	//
	// Operations
	//

	/**
	 * Submits a module identification task to the executor.
	 * 
	 * @param identifyModule
	 *        The task
	 */
	public void identifyModule( Engine.IdentifyModule identifyModule )
	{
		getPhaser().register();
		getExecutor().submit( identifyModule );
	}

	/**
	 * Checks if we are already identifying the module. If we are not, then mark
	 * that we have begun identifying it. If we are, then hook the task to run
	 * when that identification finishes.
	 * 
	 * @param identifyModule
	 *        The task to submit or run when identification finishes
	 * @return True if not already identifying the module
	 */
	public boolean beginIdentifyingIfNotIdentifying( Engine.IdentifyModule identifyModule )
	{
		return getJobs().beginIfNotBegun( identifyModule.getModule().getSpecification().hashCode(), getExecutor(), getPhaser(), identifyModule );
	}

	/**
	 * Submits the task to run when identification finishes. If already
	 * finished, runs the task now.
	 * 
	 * @param identifiedModule
	 *        The task
	 */
	public void onIdentified( Engine.IdentifiedModule identifiedModule )
	{
		getJobs().onEnd( identifiedModule.getModule().getSpecification().hashCode(), identifiedModule );
	}

	/**
	 * Mark the identification finished, which may trigger tasks to be
	 * submitted.
	 * 
	 * @param module
	 *        The module
	 */
	public void notifyIdentified( Module module )
	{
		getJobs().notifyEnd( module.getSpecification().hashCode() );
	}

	//
	// Closeable
	//

	public void close()
	{
		phaser.arriveAndAwaitAdvance();
		executor.shutdown();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ExecutorService executor;

	private final Phaser phaser = new Phaser( 1 );

	private final Jobs jobs = new Jobs();
}
