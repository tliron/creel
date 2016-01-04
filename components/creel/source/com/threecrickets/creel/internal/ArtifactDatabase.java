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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.MultiValueProperties;

/**
 * A database of artifacts that be saved to and loaded from a JVM properties
 * file.
 * 
 * @author Tal Liron
 */
public class ArtifactDatabase
{
	//
	// Construction
	//

	/**
	 * Constructor. Loads the database from a JVM properties file if it exists.
	 * 
	 * @param file
	 *        The JVM properties file
	 * @param rootDir
	 *        The root directory for artifacts
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public ArtifactDatabase( String file, String rootDir ) throws IOException
	{
		this( new File( file ), rootDir != null ? new File( rootDir ) : null );
	}

	/**
	 * Constructor. Loads the database from a JVM properties file if it exists.
	 * 
	 * @param file
	 *        The JVM properties file
	 * @param rootDir
	 *        The root directory for artifacts
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public ArtifactDatabase( File file, File rootDir ) throws IOException
	{
		try
		{
			this.file = file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new RuntimeException( "Could not access database file: " + file, x );
		}
		try
		{
			this.rootDir = rootDir != null ? rootDir.getCanonicalFile() : new File( "" ).getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new RuntimeException( "Could not access root directory: " + rootDir, x );
		}
		try
		{
			MultiValueProperties properties = new MultiValueProperties();
			properties.load( new BufferedReader( new FileReader( file ), IoUtil.bufferSize ) );

			for( Map<String, String> config : properties.toMaps() )
				addArtifact( new Artifact( config, getRootDir() ) );
		}
		catch( FileNotFoundException x )
		{
		}
	}

	//
	// Attributes
	//

	/**
	 * The database file.
	 * 
	 * @return The database file
	 */
	public File getDatabaseFile()
	{
		return file;
	}

	/**
	 * The root directory for artifacts.
	 * 
	 * @return The root directory
	 */
	public File getRootDir()
	{
		return rootDir;
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
	 */
	public void addArtifacts( Iterable<Artifact> artifacts )
	{
		for( Artifact artifact : artifacts )
			addArtifact( artifact );
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
			Map<String, Object> config = artifact.toConfig( getRootDir() );
			properties.putMap( index++, config );
		}

		Writer writer = new BufferedWriter( new FileWriter( file ), IoUtil.bufferSize );
		try
		{
			properties.store( writer, "Managed by Creel" );
		}
		finally
		{
			writer.close();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final File rootDir;

	private final Set<Artifact> artifacts = new HashSet<Artifact>();
}
