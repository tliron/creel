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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.UnsupportedPlatformException;
import com.threecrickets.creel.internal.ArtifactsClassLoader;
import com.threecrickets.creel.internal.ConcurrentIdentificationContext;
import com.threecrickets.creel.internal.Conflicts;
import com.threecrickets.creel.internal.IdentificationContext;
import com.threecrickets.creel.internal.Modules;
import com.threecrickets.creel.internal.State;
import com.threecrickets.creel.packaging.PackagingUtil;
import com.threecrickets.creel.util.ClassUtil;
import com.threecrickets.creel.util.ConfigHelper;

/**
 * The heart of Creel: installs and upgrades dependent modules.
 * <p>
 * The is done by:
 * <ul>
 * <li>identifying modules and their dependencies (multithreaded)</li>
 * <li>resolving conflicts according to policy</li>
 * <li>installing modules by downloading or copying them from repositories
 * (multithreaded)</li>
 * <li>unpacking packages (while respecting volatile files)</li>
 * <li>removing redundant files (while respecting volatile ones)</li>
 * </ul>
 * <p>
 * The engine is itself ignorant as to specific repository and module
 * technologies, here called "platforms". Those specifics are handled by classes
 * that extend {@link ModuleIdentifier}, {@link ModuleSpecification}, and
 * {@link Repository}. By default, support for Maven is installed and is used as
 * the default platform. Use {@link Engine#setPlatform(String, String)} to add
 * more.
 * <p>
 * The class is <i>not</i> thread-safe.
 * 
 * @author Tal Liron
 */
public class Engine extends Notifier implements Runnable
{
	//
	// Constants
	//

	/**
	 * Module conflict resolution policy.
	 */
	public enum ConflictPolicy
	{
		/**
		 * Choose the newest module.
		 */
		NEWEST,
		/**
		 * Choose the oldest module.
		 */
		OLDEST;

		public static ConflictPolicy valueOfNonStrict( String value )
		{
			return ClassUtil.valueOfNonStrict( ConflictPolicy.class, value );
		}
	};

	/**
	 * Run stage.
	 */
	public enum Stage
	{
		/**
		 * Identification (first stage).
		 */
		IDENTIFICATION( 1 ),
		/**
		 * Installation (second stage).
		 */
		INSTALLATION( 2 ),
		/**
		 * Unpacking (third stage).
		 */
		UNPACKING( 3 ),
		/**
		 * Delete redundant (fourth stage).
		 */
		DELETE_REDUNDANT( 4 ),
		/**
		 * All stages.
		 */
		ALL( Integer.MAX_VALUE );

		public int getValue()
		{
			return value;
		}

		public static Stage valueOf( int value )
		{
			for( Stage stage : values() )
				if( value == stage.getValue() )
					return stage;
			return null;
		}

		public static Stage valueOfNonStrict( String value )
		{
			return ClassUtil.valueOfNonStrict( Stage.class, value );
		}

		private Stage( int value )
		{
			this.value = value;
		};

		private final int value;
	};

	//
	// Static operations
	//

	/**
	 * The version of Creel.
	 * 
	 * @return The version
	 */
	public static String getVersion()
	{
		return Engine.class.getPackage().getImplementationVersion();
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * <p>
	 * By default supports the Maven platform as "mvn".
	 */
	public Engine()
	{
		super( new EventHandlers() );
		setPlatform( "mvn", "com.threecrickets.creel.maven.Maven" );
	}

	//
	// Attributes
	//

	/**
	 * The supported platforms.
	 * 
	 * @return A map of platform names to class prefixes
	 */
	public Map<String, String> getPlatforms()
	{
		return Collections.unmodifiableMap( platforms );
	}

	/**
	 * Adds support for a platform.
	 * 
	 * @param name
	 *        The platform name
	 * @param prefix
	 *        The class prefix
	 */
	public void setPlatform( String name, String prefix )
	{
		platforms.put( name, prefix );
	}

	/**
	 * The default platform to use if none is specified. Defaults to "mvn".
	 * 
	 * @return The default platform name
	 */
	public String getDefaultPlatform()
	{
		return defaultPlatform;
	}

	/**
	 * The default platform to use if none is specified. Defaults to "mvn".
	 * 
	 * @param defaultPlatform
	 *        The default platform name
	 */
	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	/**
	 * The module conflict resolution policy. Defaults to
	 * {@link ConflictPolicy#NEWEST}.
	 * 
	 * @return The conflict policy
	 */
	public ConflictPolicy getConflictPolicy()
	{
		return conflictPolicy;
	}

	/**
	 * The module conflict resolution policy. Defaults to
	 * {@link ConflictPolicy#NEWEST}.
	 * 
	 * @param conflictPolicy
	 *        The conflict policy
	 */
	public void setConflictPolicy( ConflictPolicy conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	/**
	 * The module conflict resolution policy. Defaults to
	 * {@link ConflictPolicy#NEWEST}.
	 * 
	 * @param conflictPolicy
	 *        The conflict policy: "newest" or "oldest"
	 */
	public void setConflictPolicy( String conflictPolicy )
	{
		ConflictPolicy conflictPolicyValue = ConflictPolicy.valueOfNonStrict( conflictPolicy );
		if( conflictPolicyValue == null )
			throw new RuntimeException( "Unsupported conflict policy: " + conflictPolicy );
		setConflictPolicy( conflictPolicyValue );
	}

	/**
	 * Whether we should use multiple threads when running. Defaults to true.
	 * 
	 * @return True if multithreaded
	 */
	public boolean isMultithreaded()
	{
		return multithreaded;
	}

	/**
	 * Whether we should use multiple threads when running. Defaults to true.
	 * 
	 * @param multithreaded
	 *        True if multithreaded
	 */
	public void setMultithreaded( boolean multithreaded )
	{
		this.multithreaded = multithreaded;
	}

	/**
	 * Number of threads per host. Defaults to 4.
	 * 
	 * @return Number of threads per host
	 */
	public int getThreadsPerHost()
	{
		return threadsPerHost;
	}

	/**
	 * Number of threads per host. Defaults to 4.
	 * 
	 * @param threadsPerHost
	 *        Number of threads per host
	 */
	public void setThreadsPerHost( int threadsPerHost )
	{
		this.threadsPerHost = threadsPerHost;
	}

	/**
	 * Number of chunks per file. Defaults to 4.
	 * 
	 * @return Number of chunks per file
	 */
	public int getChunksPerFile()
	{
		return chunksPerFile;
	}

	/**
	 * Number of chunks per file. Defaults to 4.
	 * 
	 * @param chunksPerFile
	 *        Number of chunks per file
	 */
	public void setChunksPerFile( int chunksPerFile )
	{
		this.chunksPerFile = chunksPerFile;
	}

	/**
	 * Stream size in bytes required to enable chunking. Defaults to 1Mb.
	 * 
	 * @return Stream size in bytes required to enable chunking
	 */
	public int getMinimumSizeForChunking()
	{
		return minimumSizeForChunking;
	}

	/**
	 * Stream size in bytes required to enable chunking. Defaults to 1Mb.
	 * 
	 * @param minimumSizeForChunking
	 *        Stream size in bytes required to enable chunking
	 */
	public void setMinimumSizeForChunking( int minimumSizeForChunking )
	{
		this.minimumSizeForChunking = minimumSizeForChunking;
	}

	/**
	 * The directories in which to install artifacts.
	 * 
	 * @return The directories
	 */
	public Directories getDirectories()
	{
		return directories;
	}

	/**
	 * Where to store state. Will default to a file named ".creel" in the other
	 * artifact root directory, or the current directory if the other artifact
	 * root directory was not set.
	 * 
	 * @return The state file
	 * @throws IOException
	 *         In case the file could not be accessed
	 */
	public File getStateFile() throws IOException
	{
		File stateFile = this.stateFile;
		if( stateFile == null )
		{
			if( getDirectories().getDefault() != null )
				stateFile = new File( getDirectories().getDefault(), ".creel" ).getCanonicalFile();
			else
				stateFile = new File( ".creel" ).getCanonicalFile();
		}
		return stateFile;
	}

	/**
	 * Where to store state. Will default to a file named ".creel" in the
	 * unknown artifact root directory, or the current directory if the unknown
	 * artifact root directory was not set.
	 * 
	 * @param statePath
	 *        The state file path or null to revert to default
	 * @throws IOException
	 *         In case the file could not be accessed
	 */
	public void setStateFile( String statePath ) throws IOException
	{
		setStateFile( statePath != null ? new File( statePath ) : null );
	}

	/**
	 * Where to store state. Will default to a file named ".creel" in the root
	 * directory.
	 * 
	 * @param stateFile
	 *        The state file or null to revert to default
	 * @throws IOException
	 *         In case the file could not be accessed
	 */
	public void setStateFile( File stateFile ) throws IOException
	{
		this.stateFile = stateFile != null ? stateFile.getCanonicalFile() : null;
	}

	/**
	 * Whether we should overwrite files. Defaults to false.
	 * 
	 * @return True to overwrite
	 */
	public boolean isOverwrite()
	{
		return overwrite;
	}

	/**
	 * Whether we should overwrite files. Defaults to false.
	 * 
	 * @param overwrite
	 *        True to overwrite
	 */
	public void setOverwrite( boolean overwrite )
	{
		this.overwrite = overwrite;
	}

	/**
	 * Whether we should use a flat file structure under the directories (no
	 * sub-directories). Defaults to false.
	 * 
	 * @return True if flat
	 */
	public boolean isFlat()
	{
		return flat;
	}

	/**
	 * Whether we should use a flat file structure under the directories (no
	 * sub-directories). Defaults to false.
	 * 
	 * @param flat
	 *        True if flat
	 */
	public void setFlat( boolean flat )
	{
		this.flat = flat;
	}

	/**
	 * The verbosity level. A higher number means more verbose. Defaults to 1.
	 * 
	 * @return The verbosity level
	 */
	public int getVerbosity()
	{
		return verbosity;
	}

	/**
	 * The verbosity level. A higher number means more verbose. Defaults to 1.
	 * 
	 * @param verbosity
	 *        The verbosity level
	 */
	public void setVerbosity( int verbosity )
	{
		this.verbosity = verbosity;
	}

	/**
	 * Delay in milliseconds to add to downloader. Used for testing/debugging.
	 * Defaults to 0.
	 * 
	 * @return The delay in milliseconds
	 */
	public int getDelay()
	{
		return delay;
	}

	/**
	 * Delay in milliseconds to add to downloader. Used for testing/debugging.
	 * Defaults to 0.
	 * 
	 * @param delay
	 *        The delay in milliseconds
	 */
	public void setDelay( int delay )
	{
		this.delay = delay;
	}

	/**
	 * The explicit modules.
	 * 
	 * @return The explicit modules
	 */
	public Iterable<Module> getExplicitModules()
	{
		return Collections.unmodifiableCollection( explicitModules );
	}

	/**
	 * Creates explicit module instances based on module specification configs.
	 * These should be set <i>before</i> calling {@link Engine#run()}.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * {@link Engine#getDefaultPlatform()}.
	 * 
	 * @param moduleSpecificationConfigs
	 *        The module specification configs
	 */
	public void setExplicitModules( Collection<Map<String, ?>> moduleSpecificationConfigs )
	{
		explicitModules.clear();
		for( Map<String, ?> config : moduleSpecificationConfigs )
		{
			ConfigHelper configHelper = new ConfigHelper( config );
			String platform = configHelper.getString( "platform", defaultPlatform );
			ModuleSpecification moduleSpecification = newModuleSpecification( platform, config );
			Module module = new Module( true, null, moduleSpecification );
			explicitModules.add( module );
		}
	}

	/**
	 * The identified modules. These will be available <i>after</i> calling
	 * {@link Engine#run()}.
	 * 
	 * @return The identified modules
	 */
	public Iterable<Module> getIdentifiedModules()
	{
		return identifiedModules;
	}

	/**
	 * The unidentified modules. These will be available <i>after</i> calling
	 * {@link Engine#run()}.
	 * 
	 * @return The unidentified modules
	 */
	public Iterable<Module> getUnidentifiedModules()
	{
		return unidentifiedModules;
	}

	/**
	 * The excluded modules. These will be available <i>after</i> calling
	 * {@link Engine#run()}.
	 * 
	 * @return The excluded modules
	 */
	public Iterable<Module> getExcludedModules()
	{
		return excludedModules;
	}

	/**
	 * The installed artifacts. These will be available <i>after</i> calling
	 * {@link Engine#run()}.
	 * 
	 * @return The installed artifacts
	 */
	public Iterable<Artifact> getInstalledArtifacts()
	{
		return Collections.unmodifiableCollection( installedArtifacts );
	}

	/**
	 * The module identification conflicts. These will be available <i>after</i>
	 * calling {@link Engine#run()}.
	 * 
	 * @return The conflicts
	 */
	public Iterable<Conflict> getConflicts()
	{
		return conflicts;
	}

	/**
	 * The repositories.
	 * 
	 * @return The repositories
	 */
	public Iterable<Repository> getRepositories()
	{
		return Collections.unmodifiableCollection( repositories );
	}

	/**
	 * Creates repository instances based on configs. These should be set
	 * <i>before</i> calling {@link Engine#run()}.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * {@link Engine#getDefaultPlatform()}.
	 * 
	 * @param repositoryConfigs
	 *        The repository configs
	 */
	public void setRepositories( Collection<Map<String, ?>> repositoryConfigs )
	{
		repositories.clear();
		for( Map<String, ?> config : repositoryConfigs )
		{
			ConfigHelper configHelper = new ConfigHelper( config );
			String platform = configHelper.getString( "platform", defaultPlatform );
			Repository repository = newRepository( platform, config );
			repositories.add( repository );
		}
	}

	/**
	 * The rules.
	 * 
	 * @return The rules
	 */
	public Iterable<Rule> getRules()
	{
		return Collections.unmodifiableCollection( rules );
	}

	/**
	 * Creates rule instances based on configs. These should be set
	 * <i>before</i> calling {@link Engine#run()}.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * {@link Engine#getDefaultPlatform()}.
	 * 
	 * @param ruleConfigs
	 *        The rule configs
	 */
	public void setRules( Collection<Map<String, ?>> ruleConfigs )
	{
		rules.clear();
		for( Map<String, ?> config : ruleConfigs )
			rules.add( new Rule( config, getDefaultPlatform() ) );
	}

	public int getIdentifiedCacheHits()
	{
		return identifiedCacheHits.get();
	}

	//
	// Operations
	//

	/**
	 * Loads the known artifacts from the state file.
	 */
	public void load()
	{
		try
		{
			for( Artifact artifact : new State( getStateFile(), getDirectories() ).getArtifacts() )
				installedArtifacts.add( artifact );
		}
		catch( FileNotFoundException x )
		{
		}
		catch( IOException x )
		{
			try
			{
				error( "Could not load state from " + getStateFile(), x );
			}
			catch( IOException xx )
			{
				error( xx );
			}
		}
	}

	/**
	 * Runs the engine up to a certain stage.
	 * 
	 * @param stage
	 *        The final stage
	 */
	public void run( int stage )
	{
		run( Stage.valueOf( stage ) );
	}

	/**
	 * Runs the engine up to a certain stage.
	 * 
	 * @param stage
	 *        The final stage
	 */
	public void run( String stage )
	{
		run( Stage.valueOfNonStrict( stage ) );
	}

	/**
	 * Runs the engine up to a certain stage.
	 * 
	 * @param stage
	 *        The final stage
	 */
	public void run( Stage stage )
	{
		info( "Creel " + getVersion() );

		State state = loadState();
		boolean stateChanged = false;

		// Identification

		if( stage.getValue() >= Stage.IDENTIFICATION.getValue() )
		{
			String id = begin( "Identifying" );

			if( isMultithreaded() )
			{
				ConcurrentIdentificationContext concurrentContext = new ConcurrentIdentificationContext( 10 );
				try
				{
					for( Module explicitModule : getExplicitModules() )
						concurrentContext.identifyModule( new IdentifyModule( explicitModule, true, concurrentContext ) );
				}
				finally
				{
					concurrentContext.close();
				}
			}
			else
			{
				for( Module explicitModule : getExplicitModules() )
					identifyModule( explicitModule, true, null );
			}

			int identifiedCount = identifiedModules.size();

			// Resolve conflicts
			conflicts.find( getIdentifiedModules() );
			conflicts.resolve( getConflictPolicy(), getVerbosity() > 0 ? this : null );
			for( Conflict conflict : getConflicts() )
			{
				for( Module reject : conflict.getRejects() )
				{
					identifiedModules.remove( reject.getIdentifier() );
					replaceModule( reject, conflict.getChosen() );
				}
			}

			// Sort for human readability
			identifiedModules.sortByIdentifiers();
			unidentifiedModules.sortBySpecifications();

			if( identifiedCount == 0 )
				end( id, "No modules identified" );
			else
				end( id, "Made " + identifiedCount + ( identifiedCount != 1 ? " identifications" : " identification" ) );
		}

		// Installation

		if( stage.getValue() >= Stage.INSTALLATION.getValue() )
		{
			if( !getIdentifiedModules().iterator().hasNext() )
			{
				deleteState();
				throw new RuntimeException( "Cannot install because no modules have been identified" );
			}

			if( getUnidentifiedModules().iterator().hasNext() )
			{
				deleteState();
				throw new RuntimeException( "Cannot install because could not identify all modules" );
			}

			String installingId = begin( "Installing" );

			Downloader downloader = new Downloader( isMultithreaded() ? getThreadsPerHost() : 1, isMultithreaded() ? getChunksPerFile() : 1, getMinimumSizeForChunking(), this );
			try
			{
				downloader.setDelay( getDelay() );
				for( Module module : identifiedModules )
				{
					for( Artifact artifact : module.getIdentifier().getArtifacts( getDirectories(), isFlat() ) )
					{
						if( isOverwrite() || !artifact.getFile().exists() )
							// Download and validate
							downloader.submit( artifact.getSourceUrl(), artifact.getFile(), module.getIdentifier().getRepository().validateArtifactTask( module.getIdentifier(), artifact, this ) );
						else
							// Only validate
							downloader.submit( module.getIdentifier().getRepository().validateArtifactTask( module.getIdentifier(), artifact, this ) );
						installedArtifacts.add( artifact );
					}
				}
				downloader.waitUntilDone();
			}
			finally
			{
				downloader.close();
			}

			int errorCount = 0;
			for( Iterator<Throwable> i = downloader.getExceptions().iterator(); i.hasNext(); i.next() )
				errorCount++;

			if( errorCount > 0 )
			{
				deleteState(); // TODO: good idea?
				String message = "Had " + errorCount + ( errorCount != 1 ? " errors during installation" : " error during installation" );
				fail( installingId, message );
				throw new RuntimeException( message );
			}

			int installedCount = downloader.getCount();

			// Unpacking

			if( ( stage.getValue() >= Stage.UNPACKING.getValue() ) && ( getDirectories().getDefault() != null ) )
			{
				ClassLoader classLoader = new ArtifactsClassLoader( getInstalledArtifacts() );

				Iterable<com.threecrickets.creel.packaging.Package> packages = null;
				try
				{
					packages = PackagingUtil.getPackages( classLoader, getDirectories().getDefault() );
				}
				catch( IOException x )
				{
					error( "Could not scan for packages", x );
				}

				if( packages != null )
				{
					for( com.threecrickets.creel.packaging.Package thePackage : packages )
					{
						if( !thePackage.iterator().hasNext() )
							continue;

						String unpackingId = begin( "Unpacking " + thePackage.getSourceFile() );
						try
						{
							int unpackedCount = 0;
							for( Artifact artifact : thePackage )
							{
								boolean copy = false;

								if( isOverwrite() || !artifact.exists() )
									copy = true;
								else
								{
									Artifact knownArtifact = state != null ? state.getArtifact( artifact.getFile() ) : null;
									if( knownArtifact == null )
										copy = true;
									else
									{
										if( !knownArtifact.isVolatile() )
											copy = artifact.isDifferent();
										else
										{
											if( !knownArtifact.wasModified() )
												copy = artifact.isDifferent();
											else
												info( "Modified, so not overwriting " + artifact.getFile() );
										}
									}
								}

								if( copy )
								{
									artifact.copy( null );
									if( artifact.isVolatile() )
										artifact.updateDigest();
									if( getVerbosity() > 1 )
										info( "Unpacked " + artifact.getFile() );
									unpackedCount++;
									installedCount++;
								}

								installedArtifacts.add( artifact );
							}

							if( unpackedCount == 0 )
								end( unpackingId, "No new files to unpack from " + thePackage.getSourceFile() );
							else
								end( unpackingId, "Unpacked " + unpackedCount + ( unpackedCount != 1 ? " new files from " : " file from " ) + thePackage.getSourceFile() );
						}
						catch( IOException x )
						{
							fail( unpackingId, "Could not unpack " + thePackage.getSourceFile(), x );
						}
					}

					// Run installers

					for( com.threecrickets.creel.packaging.Package thePackage : packages )
					{
						String installer = thePackage.getInstaller();
						if( installer != null )
						{
							try
							{
								ClassUtil.main( classLoader, installer.split( " " ) );
							}
							catch( Throwable x )
							{
								error( "Could not run installer: " + installer, x );
							}
						}
					}

					// TODO: uninstallers?
				}
			}

			if( installedCount == 0 )
				end( installingId, "No new artifacts to install" );
			else
				end( installingId, "Installed " + installedCount + ( installedCount != 1 ? " new artifacts" : " new artifact" ) );
		}

		// Delete redundant

		if( ( stage.getValue() >= Stage.DELETE_REDUNDANT.getValue() ) && ( state != null ) )
		{
			Iterable<Artifact> redundantArtifacts = state.getRedundantArtifacts( getInstalledArtifacts() );
			if( redundantArtifacts.iterator().hasNext() )
			{
				String id = begin( "Deleting redundant artifacts" );

				int deletedCount = 0;

				for( Artifact redundantArtifact : redundantArtifacts )
				{
					boolean delete = false;

					if( !redundantArtifact.isVolatile() )
						delete = true;
					else
					{
						try
						{
							if( !redundantArtifact.wasModified() )
								delete = true;
							else
								info( "Modified, so not deleting " + redundantArtifact.getFile() );
						}
						catch( IOException x )
						{
							error( "Could not access " + redundantArtifact.getFile() );
						}
					}

					if( delete )
					{
						if( redundantArtifact.delete( getDirectories() ) )
						{
							if( getVerbosity() > 1 )
								info( "Deleted " + redundantArtifact.getFile() );
							if( state.removeArtifact( redundantArtifact ) )
								stateChanged = true;
							deletedCount++;
						}
						else
							error( "Could not delete " + redundantArtifact.getFile() );
					}
				}

				if( deletedCount > 0 )
					end( id, "Deleted " + deletedCount + ( deletedCount != 1 ? " redundant artifacts" : " redundant artifact" ) );
				else
					end( id, "No redundant artifacts to delete" );
			}
		}

		if( state.addArtifacts( getInstalledArtifacts() ) )
			stateChanged = true;

		if( stateChanged )
			saveState( state );
	}

	//
	// Runnable
	//

	public void run()
	{
		run( Stage.ALL );
	}

	//
	// Classes
	//

	public class IdentifyModule implements Runnable
	{
		public IdentifyModule( Module module, boolean recursive, ConcurrentIdentificationContext concurrentContext )
		{
			this.module = module;
			this.recursive = recursive;
			this.concurrentContext = concurrentContext;
		}

		public Module getModule()
		{
			return module;
		}

		public void run()
		{
			try
			{
				identifyModule( module, recursive, concurrentContext );
			}
			catch( Throwable x )
			{
				error( "Identification error for " + module.getSpecification() + ": " + x.getMessage(), x );
			}
			concurrentContext.getPhaser().arriveAndDeregister();
		}

		private final Module module;

		private final boolean recursive;

		private final ConcurrentIdentificationContext concurrentContext;
	}

	public class IdentifiedModule implements Runnable
	{
		public IdentifiedModule( Module module, String id )
		{
			this.module = module;
			this.id = id;
		}

		public Module getModule()
		{
			return module;
		}

		public void run()
		{
			Module identifiedModule = identifiedModules.get( module.getSpecification() );
			if( id != null )
			{
				if( identifiedModule != null )
					end( id, "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
				else
					fail( id, "Could not identify " + module.getSpecification() );
			}
		}

		private final Module module;

		private final String id;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, String> platforms = new HashMap<String, String>();

	private String defaultPlatform = "mvn";

	private ConflictPolicy conflictPolicy = ConflictPolicy.NEWEST;

	private boolean multithreaded = true;

	private int threadsPerHost = 4;

	private int chunksPerFile = 4;

	private int minimumSizeForChunking = 1024 * 1024;

	private Directories directories = new Directories();

	private File stateFile;

	private boolean overwrite;

	private boolean flat;

	private int verbosity = 1;

	private int delay;

	private final List<Module> explicitModules = new ArrayList<Module>();

	private final Modules identifiedModules = new Modules();

	private final Modules unidentifiedModules = new Modules();

	private final Modules excludedModules = new Modules();

	private final Collection<Artifact> installedArtifacts = new ArrayList<Artifact>();

	private final Conflicts conflicts = new Conflicts();

	private final Collection<Repository> repositories = new ArrayList<Repository>();

	private final Collection<Rule> rules = new ArrayList<Rule>();

	private final AtomicInteger identifiedCacheHits = new AtomicInteger();

	private void identifyModule( final Module module, final boolean recursive, final ConcurrentIdentificationContext concurrentContext )
	{
		IdentificationContext context = new IdentificationContext( getRepositories(), recursive );

		applyRules( module, context );

		if( context.isExclude() )
			return;

		if( module.getIdentifier() != null )
		{
			// Nothing to do: already identified
		}
		else if( unidentifiedModules.get( module.getSpecification() ) != null )
		{
			// Nothing to do: already failed to identify this specification,
			// no use trying again
		}
		else
		{
			// Check to see if we've already identified it
			Module identifiedModule = identifiedModules.get( module.getSpecification() );
			if( identifiedModule == null )
			{
				if( concurrentContext != null )
				{
					boolean alreadyIdentifying = !concurrentContext.beginIdentifyingIfNotIdentifying( new IdentifyModule( module, recursive, concurrentContext ) );
					if( alreadyIdentifying )
					{
						// Another thread is already in the process of
						// identifying this specification, so we'll wait for
						// them to finish
						final String id = getVerbosity() > 1 ? begin( "Waiting for identification of " + module.getSpecification() ) : null;
						concurrentContext.onIdentified( new IdentifiedModule( module, id ) );
						return;
					}
				}

				String id = begin( "Identifying " + module.getSpecification() );

				// Gather allowed module identifiers from all repositories
				Set<ModuleIdentifier> allowedModuleIdentifiers = new LinkedHashSet<ModuleIdentifier>();
				for( Repository repository : context.getRepositories() )
					for( ModuleIdentifier allowedModuleIdentifier : repository.getAllowedModuleIdentifiers( module.getSpecification(), this ) )
						allowedModuleIdentifiers.add( allowedModuleIdentifier );

				// Pick the best module identifier
				if( !allowedModuleIdentifiers.isEmpty() )
				{
					LinkedList<ModuleIdentifier> moduleIdentifiers = new LinkedList<ModuleIdentifier>( allowedModuleIdentifiers );
					Collections.sort( moduleIdentifiers );

					// Best module is last (newest)
					ModuleIdentifier moduleIdentifier = moduleIdentifiers.getLast();
					identifiedModule = moduleIdentifier.getRepository().getModule( moduleIdentifier, this );

					if( identifiedModule != null )
						end( id, "Identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
					else
						fail( id, "Could not get module " + moduleIdentifier + " from " + moduleIdentifier.getRepository().getId() + " repository" );
				}
				else
					fail( id, "Could not identify " + module.getSpecification() );
			}
			else
			{
				if( getVerbosity() > 1 )
					info( "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
				identifiedCacheHits.incrementAndGet();
			}

			if( identifiedModule != null )
				module.copyIdentificationFrom( identifiedModule );
		}

		addModule( module );

		if( concurrentContext != null )
			concurrentContext.notifyIdentified( module );

		if( context.isRecursive() )
		{
			// Identify dependencies recursively
			if( concurrentContext != null )
			{
				for( Module dependency : module.getDependencies() )
					concurrentContext.identifyModule( new IdentifyModule( dependency, true, concurrentContext ) );
			}
			else
			{
				for( Module dependency : module.getDependencies() )
					identifyModule( dependency, true, null );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void applyRules( Module module, IdentificationContext context )
	{
		for( Rule rule : getRules() )
		{
			Command command = null;

			// Try repositories
			for( Repository repository : getRepositories() )
			{
				command = repository.applyRule( module, rule, this );
				if( command != null )
					break;
			}

			if( command == null )
			{
				error( "Unsupported rule: " + rule.getType() );
				continue;
			}

			if( "handled".equals( command.getType() ) )
				continue;

			// Do command
			if( "excludeModule".equals( command.getType() ) )
			{
				if( excludedModules.addBySpecification( module ) )
					if( getVerbosity() > 0 )
						info( "Excluded " + module.getSpecification() );
				context.setExclude( true );
			}
			else if( "excludeDependencies".equals( command.getType() ) )
			{
				if( getVerbosity() > 0 )
					info( "Excluded dependencies for " + module.getSpecification() );
				context.setRecursive( false );
			}
			else if( "setRepositories".equals( command.getType() ) )
			{
				StringBuilder ids = new StringBuilder();
				context.getRepositories().clear();
				for( String id : (Iterable<String>) command.get( "repositories" ) )
					for( Repository repository : getRepositories() )
						if( id.equals( repository.getId() ) )
						{
							context.getRepositories().add( repository );
							if( ids.length() != 0 )
								ids.append( ", " );
							ids.append( id );
						}
				if( ( getVerbosity() > 0 ) && !context.getRepositories().isEmpty() )
					info( "Forced " + module.getSpecification() + " to " + ids + ( context.getRepositories().size() != 1 ? " repositories" : " repository" ) );
			}
			else
				error( "Unsupported command: " + command.getType() );
		}
	}

	private void addModule( Module module )
	{
		if( module.getIdentifier() != null )
			identifiedModules.addByIdentifier( module );
		else
			unidentifiedModules.addBySpecification( module );
	}

	private void replaceModule( Module oldModule, Module newModule )
	{
		for( ListIterator<Module> i = explicitModules.listIterator(); i.hasNext(); )
		{
			Module explicitModule = i.next();
			if( ( explicitModule.getIdentifier() != null ) && ( explicitModule.getIdentifier().equals( oldModule.getIdentifier() ) ) )
			{
				explicitModule = newModule;
				i.set( explicitModule );
				explicitModule.setExplicit( true );
			}
			explicitModule.replaceModule( oldModule, newModule, true );
		}
	}

	private State loadState()
	{
		try
		{
			State state = new State( getStateFile(), getDirectories() );
			if( getVerbosity() > 0 )
				info( "Loaded state from " + getStateFile() );
			return state;
		}
		catch( FileNotFoundException x )
		{
		}
		catch( IOException x )
		{
			try
			{
				throw new RuntimeException( "Could not load state from " + getStateFile(), x );
			}
			catch( IOException xx )
			{
				throw new RuntimeException( xx );
			}
		}
		return null;
	}

	private void deleteState()
	{
		if( getUnidentifiedModules().iterator().hasNext() )
		{
			try
			{
				getStateFile().delete();
			}
			catch( FileNotFoundException x )
			{
			}
			catch( IOException x )
			{
				try
				{
					throw new RuntimeException( "Could not delete state at " + getStateFile(), x );
				}
				catch( IOException xx )
				{
					throw new RuntimeException( xx );
				}
			}
		}
	}

	private void saveState( State state )
	{
		try
		{
			state.save();
			if( getVerbosity() > 0 )
				info( "Saved state to " + getStateFile() );
		}
		catch( IOException x )
		{
			try
			{
				throw new RuntimeException( "Could not save state to " + getStateFile(), x );
			}
			catch( IOException xx )
			{
				throw new RuntimeException( xx );
			}
		}
	}

	private Repository newRepository( String platform, Map<String, ?> config )
	{
		return newInstance( platform, Repository.class.getSimpleName(), config );
	}

	private ModuleSpecification newModuleSpecification( String platform, Map<String, ?> config )
	{
		return newInstance( platform, ModuleSpecification.class.getSimpleName(), config );
	}

	private <T> T newInstance( String platform, String baseClassName, Map<String, ?> config )
	{
		String prefix = platforms.get( platform );
		if( prefix == null )
			throw new UnsupportedPlatformException( platform );
		String className = prefix + baseClassName;
		return ClassUtil.newInstance( className, config );
	}
}
