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
 * @author Tal Liron
 */
public class ArtifactDatabase
{
	//
	// Construction
	//

	public ArtifactDatabase( String databaseFile, String rootDir ) throws IOException
	{
		this( new File( databaseFile ), rootDir != null ? new File( rootDir ) : null );
	}

	public ArtifactDatabase( File databaseFile, File rootDir ) throws IOException
	{
		try
		{
			this.databaseFile = databaseFile.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new RuntimeException( "Could not access database file: " + databaseFile, x );
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
			properties.load( new BufferedReader( new FileReader( databaseFile ), IoUtil.bufferSize ) );

			for( Map<String, String> config : properties.toMaps() )
			{
				try
				{
					addArtifact( new Artifact( config, getRootDir() ) );
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

	public File getDatabaseFile()
	{
		return databaseFile;
	}

	public File getRootDir()
	{
		return rootDir;
	}

	public Artifact getArtifact( File file )
	{
		for( Artifact artifact : getArtifacts() )
			if( file.equals( artifact.getFile() ) )
				return artifact;
		return null;
	}

	public Iterable<Artifact> getArtifacts()
	{
		return Collections.unmodifiableCollection( artifacts );
	}

	public boolean addArtifact( Artifact artifact )
	{
		return artifacts.add( artifact );
	}

	public void addArtifacts( Iterable<Artifact> artifacts )
	{
		for( Artifact artifact : artifacts )
			addArtifact( artifact );
	}

	public boolean removeArtifact( Artifact artifact )
	{
		return artifacts.remove( artifact );
	}

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

	public void save() throws IOException
	{
		Files.createDirectories( databaseFile.toPath().getParent() );

		MultiValueProperties properties = new MultiValueProperties();
		int index = 0;
		for( Artifact artifact : getArtifacts() )
		{
			Map<String, Object> config = artifact.toConfig( getRootDir() );
			properties.putMap( index++, config );
		}

		Writer writer = new BufferedWriter( new FileWriter( databaseFile ), IoUtil.bufferSize );
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

	private final File databaseFile;

	private final File rootDir;

	private final Set<Artifact> artifacts = new HashSet<Artifact>();
}
