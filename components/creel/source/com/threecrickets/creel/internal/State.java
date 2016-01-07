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

package com.threecrickets.creel.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Directories;
import com.threecrickets.creel.Engine;
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
	 * @param directories
	 *        The directories in which to install artifacts
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public State( File file, Directories directories ) throws IOException
	{
		try
		{
			this.file = file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new RuntimeException( "Could not access properties file: " + file, x );
		}

		this.directories = directories;

		try
		{
			MultiValueProperties properties = new MultiValueProperties();
			properties.load( new BufferedReader( new FileReader( file ), IoUtil.bufferSize ) );

			for( Map<String, String> config : properties.toMaps() )
			{
				try
				{
					addArtifact( new Artifact( config, directories ) );
				}
				catch( RuntimeException x )
				{
				}
			}
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

	//
	// Operations
	//

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
	 * The artifacts in the database.
	 * 
	 * @return The artifacts
	 */
	public Iterable<Artifact> getArtifacts()
	{
		return Collections.unmodifiableCollection( artifacts );
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
		for( Artifact artifact : getArtifacts() )
		{
			Map<String, Object> config = artifact.toConfig( getDirectories() );
			properties.putMap( index++, config );
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

	private final SortedSet<Artifact> artifacts = new TreeSet<Artifact>();
}
