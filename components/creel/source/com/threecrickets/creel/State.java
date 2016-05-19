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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.MultiValueProperties;

/**
 * A database of artifacts that be saved to and loaded from a JVM properties
 * file.
 * 
 * @author Tal Liron
 */
public class State
{
	//
	// Construction
	//

	/**
	 * Constructor. Loads the database from a JVM properties file if it exists.
	 * 
	 * @param file
	 *        The JVM properties file
	 * @param factory
	 *        The factory
	 * @param directories
	 *        The directories in which to install artifacts
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public State( File file, Factory factory, Directories directories ) throws IOException
	{
		try
		{
			this.file = file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not access properties file: " + file, x );
		}

		this.directories = directories;

		try
		{
			MultiValueProperties properties = new MultiValueProperties();
			Reader reader = new BufferedReader( new FileReader( file ), IoUtil.bufferSize );
			try
			{
				properties.load( reader );
			}
			finally
			{
				reader.close();
			}

			for( Map<String, String> config : properties.toMaps( "module" ) )
				addModule( new Module( factory, config ) );

			organizeModules();

			for( Map<String, String> config : properties.toMaps( "artifact" ) )
				addArtifact( new Artifact( config, directories ) );
		}
		catch( FileNotFoundException x )
		{
		}
	}

	//
	// Attributes
	//

	/**
	 * The state file.
	 * 
	 * @return The state file
	 */
	public File getFile()
	{
		return file;
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
	 * The modules in the database.
	 * 
	 * @return The modules
	 */
	public Iterable<Module> getModules()
	{
		return Collections.unmodifiableCollection( modules );
	}

	/**
	 * The artifacts in the database.
	 * 
	 * @return The artifacts
	 */
	public Iterable<Artifact> getArtifacts()
	{
		return Collections.unmodifiableCollection( artifacts );
	}

	/**
	 * Gets an artifact from the database if it already is there.
	 * 
	 * @param file
	 *        The file (should be canonical)
	 * @return The artifact or null if not found
	 */
	public Artifact getArtifact( File file )
	{
		for( Artifact artifact : getArtifacts() )
			if( file.equals( artifact.getFile() ) )
				return artifact;
		return null;
	}

	/**
	 * Gets all artifacts in the database that are <i>not</i> listed.
	 * 
	 * @param allArtifacts
	 *        The listed artifacts
	 * @return The redundant artifacts
	 */
	public Iterable<Artifact> getRedundantArtifacts( Iterable<Artifact> allArtifacts )
	{
		Collection<Artifact> reundantArtifacts = new LinkedList<Artifact>();
		for( Artifact artifact : getArtifacts() )
			reundantArtifacts.add( artifact );
		for( Artifact artifact : allArtifacts )
			reundantArtifacts.remove( artifact );
		return Collections.unmodifiableCollection( reundantArtifacts );
	}

	//
	// Operations
	//

	/**
	 * Adds a module to the database.
	 * <p>
	 * The module must be identified.
	 * 
	 * @param module
	 *        The module
	 * @return True if added, false if already in database
	 */
	public boolean addModule( Module module )
	{
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		for( Module m : getModules() )
			if( moduleIdentifier.equals( m.getIdentifier() ) )
				return false;
		modules.add( module );
		return true;
	}

	/**
	 * Adds modules to the database.
	 * <p>
	 * The modules must be identified.
	 * 
	 * @param modules
	 *        The modules
	 * @return True if anything was added
	 */
	public boolean addModules( Iterable<Module> modules )
	{
		boolean added = false;
		for( Module module : modules )
			if( addModule( module ) )
				added = true;
		return added;
	}

	/**
	 * Organizes the added modules into a tree structure.
	 */
	public void organizeModules()
	{
		// Replace all supplicants with existing instances
		for( Module module : modules )
			for( Module supplicant : module.getSupplicants() )
			{
				ModuleIdentifier supplicantIdentifier = supplicant.getIdentifier();
				for( Module m : modules )
					if( supplicantIdentifier.equals( m.getIdentifier() ) )
					{
						module.removeSupplicant( supplicant );
						module.addSupplicant( m );
					}
			}

		// Match supplicants with dependents
		for( Module module : modules )
			for( Module supplicant : module.getSupplicants() )
				supplicant.addDependency( module );

		// Set our modules just to the explicit ones
		ArrayList<Module> copy = new ArrayList<Module>( modules );
		modules.clear();
		for( Module module : copy )
			if( module.isExplicit() )
				modules.add( module );
	}

	/**
	 * Adds an artifact to the database.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return True if added, false if already in database
	 */
	public boolean addArtifact( Artifact artifact )
	{
		return artifacts.add( artifact );
	}

	/**
	 * Adds artifacts to the database.
	 * 
	 * @param artifacts
	 *        The artifacts
	 * @return True if anything was added
	 */
	public boolean addArtifacts( Iterable<Artifact> artifacts )
	{
		boolean added = false;
		for( Artifact artifact : artifacts )
			if( addArtifact( artifact ) )
				added = true;
		return added;
	}

	/**
	 * Removes an artifact from the database.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return True if removed, false is not in database
	 */
	public boolean removeArtifact( Artifact artifact )
	{
		return artifacts.remove( artifact );
	}

	/**
	 * Saves the database to the JVM properties file.
	 * 
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public void save() throws IOException
	{
		Files.createDirectories( file.toPath().getParent() );

		MultiValueProperties properties = new MultiValueProperties();

		int index = 0;
		for( Module module : getModules() )
		{
			Map<String, Object> config = module.toConfig();
			properties.putMap( "module", index++, config );
		}
		index = 0;
		for( Artifact artifact : getArtifacts() )
		{
			Map<String, Object> config = artifact.toConfig( getDirectories() );
			properties.putMap( "artifact", index++, config );
		}

		Writer writer = new BufferedWriter( new FileWriter( file ), IoUtil.bufferSize );
		try
		{
			properties.store( writer, "Managed by Creel " + Engine.getVersion() );
		}
		finally
		{
			writer.close();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final Directories directories;

	private final ArrayList<Module> modules = new ArrayList<Module>();

	private final SortedSet<Artifact> artifacts = new TreeSet<Artifact>();
}
