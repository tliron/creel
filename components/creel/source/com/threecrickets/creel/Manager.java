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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.downloader.internal.ConcurrentIdentificationContext;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.UnsupportedPlatformException;
import com.threecrickets.creel.internal.Conflicts;
import com.threecrickets.creel.internal.IdentificationContext;
import com.threecrickets.creel.internal.Modules;
import com.threecrickets.creel.util.ClassUtil;
import com.threecrickets.creel.util.ConfigHelper;

/**
 * Handles identifying and installing modules.
 * 
 * @author Tal Liron
 */
public class Manager extends Notifier
{
	//
	// Enums
	//

	public enum ConflictPolicy
	{
		NEWEST, OLDEST
	};

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
			concurrentContext.identified();
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

		public void run()
		{
			Module identifiedModule = identifiedModules.get( module.getSpecification() );
			if( identifiedModule != null )
				end( id, "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
			else
				fail( id, "Could not identify " + module.getSpecification() );
		}

		private final Module module;

		private final String id;
	}

	//
	// Construction
	//

	public Manager()
	{
		super( new EventHandlers() );
		setPlatform( "maven", "com.threecrickets.creel.maven" );
	}

	//
	// Attributes
	//

	public Map<String, String> getPlatforms()
	{
		return Collections.unmodifiableMap( platforms );
	}

	public void setPlatform( String name, String packageName )
	{
		platforms.put( name, packageName );
	}

	public String getDefaultPlatform()
	{
		return defaultPlatform;
	}

	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	public ConflictPolicy getConflictPolicy()
	{
		return conflictPolicy;
	}

	public void setConflictPolicy( ConflictPolicy conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	public void setConflictPolicy( String conflictPolicy )
	{
		if( "newest".equalsIgnoreCase( conflictPolicy ) )
			setConflictPolicy( Manager.ConflictPolicy.NEWEST );
		else if( "oldest".equalsIgnoreCase( conflictPolicy ) )
			setConflictPolicy( Manager.ConflictPolicy.OLDEST );
		else
			throw new RuntimeException( "Unsupported conflict policy: " + conflictPolicy );
	}

	public boolean isMultithreaded()
	{
		return multithreaded;
	}

	public void setMultithreaded( boolean multithreaded )
	{
		this.multithreaded = multithreaded;
	}

	public Iterable<Module> getExplicitModules()
	{
		return Collections.unmodifiableCollection( explicitModules );
	}

	/**
	 * Creates module instances based on module specification configs.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * defaultPlatform.
	 * 
	 * @param moduleSpecificationConfigs
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

	public Iterable<Repository> getRepositories()
	{
		return Collections.unmodifiableCollection( repositories );
	}

	/**
	 * Creates repository instances based on configs.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * defaultPlatform.
	 * 
	 * @param repositoryConfigs
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

	public Iterable<Rule> getRules()
	{
		return Collections.unmodifiableCollection( rules );
	}

	public void setRules( Collection<Map<String, ?>> ruleConfigs )
	{
		rules.clear();
		for( Map<String, ?> config : ruleConfigs )
			rules.add( new Rule( config, getDefaultPlatform() ) );
	}

	public Iterable<Module> getIdentifiedModules()
	{
		return identifiedModules;
	}

	public Iterable<Module> getUnidentifiedModules()
	{
		return unidentifiedModules;
	}

	public Iterable<Conflict> getConflicts()
	{
		return conflicts;
	}

	public int getIdentifiedCacheHits()
	{
		return identifiedCacheHits.get();
	}

	public void addModule( Module module )
	{
		if( module.getIdentifier() != null )
			identifiedModules.addByIdentifier( module );
		else
			unidentifiedModules.addBySpecification( module );
	}

	public void replaceModule( Module oldModule, Module newModule )
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

	//
	// Operations
	//

	public Repository newRepository( String platform, Map<String, ?> config )
	{
		return newInstance( platform, Repository.class.getSimpleName(), config );
	}

	public ModuleSpecification newModuleSpecification( String platform, Map<String, ?> config )
	{
		return newInstance( platform, ModuleSpecification.class.getSimpleName(), config );
	}

	/**
	 * Goes over explicitModules and identifies them recursively. This is done
	 * using fork/join parallelism for better efficiency.
	 * <p>
	 * When finished, identifiedModules and unidentifiedModules would be filled
	 * appropriately.
	 */
	public void identify()
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

		int count = identifiedModules.size();

		resolveConflicts();

		identifiedModules.sortByIdentifiers();
		unidentifiedModules.sortBySpecifications();

		end( id, "Made " + count + ( count != 1 ? " identifications" : " identification" ) );
	}

	/**
	 * Identifies a module, optionally identifying its dependencies recursively
	 * (supporting efficient multithreaded parallelism that avoids repeating
	 * work already done).
	 * <p>
	 * "Identification" means finding the best identifier available from all the
	 * candidates in all the repositories that match the specification. A
	 * successful identification results in the the module has an identifier. An
	 * unidentified module has only a specification, but no identifier.
	 * <p>
	 * A cache of identified modules is maintained in the manager to avoid
	 * identifying the same module twice.
	 * 
	 * @param module
	 * @param recursive
	 * @param concurrentContext
	 */
	public void identifyModule( final Module module, final boolean recursive, final ConcurrentIdentificationContext concurrentContext )
	{
		IdentificationContext context = new IdentificationContext( getRepositories(), recursive );

		applyRules( module, context );

		if( context.isExclude() )
		{
			// Mark to not identify this specification
			unidentifiedModules.addBySpecification( module );
		}
		else
		{
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
						boolean alreadyIdentifying = !concurrentContext.beginIdentifyingIfNotIdentifying( module, new IdentifyModule( module, recursive, concurrentContext ) );
						if( alreadyIdentifying )
						{
							// Another thread is already in the process of
							// identifying this specification, so we'll wait for
							// them to finish
							final String id = begin( "Waiting for identification of " + module.getSpecification() );
							concurrentContext.onIdentified( module, new IdentifiedModule( module, id ) );
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
					debug( "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
					identifiedCacheHits.incrementAndGet();
				}

				if( identifiedModule != null )
					module.copyIdentificationFrom( identifiedModule );
			}

			addModule( module );
		}

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
		else
		{
			// Add dependencies as is (unidentified)
			for( Module dependency : module.getDependencies() )
				addModule( dependency );
		}
	}

	public void findConflicts()
	{
		conflicts.find( getIdentifiedModules() );
	}

	public void resolveConflicts()
	{
		findConflicts();
		conflicts.resolve( getConflictPolicy(), this );
		for( Conflict conflict : getConflicts() )
			for( Module reject : conflict.getRejects() )
			{
				identifiedModules.remove( reject.getIdentifier() );
				replaceModule( reject, conflict.getChosen() );
			}
	}

	public Iterable<Artifact> install( String directory, boolean overwrite, boolean flat )
	{
		return install( new File( directory ), overwrite, flat );
	}

	public Iterable<Artifact> install( File directory, boolean overwrite, boolean flat )
	{
		Collection<Artifact> artifacts = new ArrayList<Artifact>();

		String id = begin( "Installing" );

		int count;
		if( isMultithreaded() )
		{
			Downloader downloader = new Downloader( 4, 4, this );
			try
			{
				downloader.setDelay( 100 );
				for( Module module : identifiedModules )
					for( Artifact artifact : module.getIdentifier().getArtifacts( directory, flat ) )
					{
						if( overwrite || !artifact.getFile().exists() )
							downloader.submit( artifact.getSourceUrl(), artifact.getFile(),
								module.getIdentifier().getRepository().validateFileTask( module.getIdentifier(), artifact.getFile(), this, downloader.getPhaser() ) );
						else
							module.getIdentifier().getRepository().validateFile( module.getIdentifier(), artifact.getFile(), this );
						artifacts.add( artifact );
					}
				downloader.waitUntilDone();
			}
			finally
			{
				downloader.close();
				count = downloader.getCount();
			}
		}
		else
		{
			// TODO
			count = 0;
		}

		end( id, "Installed " + count + ( count != 1 ? " artifacts" : " artifact" ) );

		return Collections.unmodifiableCollection( artifacts );
	}

	@SuppressWarnings("unchecked")
	public void applyRules( Module module, IdentificationContext context )
	{
		for( Rule rule : getRules() )
		{
			Command command = null;

			// Try repositories
			for( Repository repository : getRepositories() )
			{
				command = repository.applyModuleRule( module, rule, this );
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
				info( "Excluded " + module.getSpecification() );
				context.setExclude( true );
			}
			else if( "excludeDependencies".equals( command.getType() ) )
			{
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
							if( ids.length() == 0 )
								ids.append( ", " );
							ids.append( id );
						}
				if( !context.getRepositories().isEmpty() )
					info( "Forced " + module.getSpecification() + " to identify in " + ids + ( context.getRepositories().size() != 1 ? " repositories" : " repository" ) );
			}
			else
				error( "Unsupported command: " + command.getType() );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, String> platforms = new HashMap<String, String>();

	private String defaultPlatform = "maven";

	private ConflictPolicy conflictPolicy = ConflictPolicy.NEWEST;

	private boolean multithreaded = true;

	private final List<Module> explicitModules = new ArrayList<Module>();

	private final Collection<Repository> repositories = new ArrayList<Repository>();

	private final Collection<Rule> rules = new ArrayList<Rule>();

	private final Modules identifiedModules = new Modules();

	private final Modules unidentifiedModules = new Modules();

	private final Conflicts conflicts = new Conflicts();

	private final AtomicInteger identifiedCacheHits = new AtomicInteger();

	private <T> T newInstance( String platform, String baseClassName, Map<String, ?> config )
	{
		String packageName = platforms.get( platform );
		if( packageName == null )
			throw new UnsupportedPlatformException();
		String className = platform.substring( 0, 1 ).toUpperCase() + platform.substring( 1 ) + baseClassName;
		className = packageName + '.' + className;
		return ClassUtil.newInstance( className, config );
	}
}
