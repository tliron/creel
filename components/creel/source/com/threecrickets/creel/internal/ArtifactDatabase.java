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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
		this( new File( databaseFile ).getCanonicalFile(), rootDir != null ? new File( rootDir ).getCanonicalFile() : null );
	}

	public ArtifactDatabase( File databaseFile, File rootDir ) throws IOException
	{
		this.databaseFile = databaseFile;
		this.rootDir = rootDir != null ? rootDir : new File( "" ).getCanonicalFile();
		try
		{
			MultiValueProperties properties = new MultiValueProperties();
			properties.load( new BufferedReader( new FileReader( databaseFile ), IoUtil.BUFFER_SIZE ) );

			for( Map<String, String> config : properties.toMaps() )
			{
				String urlValue = config.get( "url" );
				if( urlValue == null )
					continue;
				String fileValue = config.get( "file" );
				if( fileValue == null )
					continue;
				URL url = new URL( urlValue );
				File file = rootDir != null ? new File( rootDir, fileValue ) : new File( fileValue );
				Artifact artifact = new Artifact( file.getCanonicalFile(), url );
				addArtifact( artifact );
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

	public Iterable<Artifact> getArtifacts()
	{
		return Collections.unmodifiableCollection( artifacts );
	}

	public boolean addArtifact( Artifact artifact ) throws IOException
	{
		return artifacts.add( artifact );
	}

	public void addArtifacts( Iterable<Artifact> artifacts ) throws IOException
	{
		for( Artifact artifact : artifacts )
			addArtifact( artifact );
	}

	public boolean removeArtifact( Artifact artifact ) throws IOException
	{
		return artifacts.remove( artifact );
	}

	public Iterable<Artifact> getRedundantArtifacts( Iterable<Artifact> allArtifacts )
	{
		Collection<Artifact> reundantArtifacts = new ArrayList<Artifact>();
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

		Path rootPath = getRootDir().toPath();

		MultiValueProperties properties = new MultiValueProperties();

		int index = 0;
		for( Artifact artifact : getArtifacts() )
		{
			properties.put( index, "url", artifact.getSourceUrl().toString() );
			Path path = artifact.getFile().toPath();
			try
			{
				properties.put( index, "file", rootPath.relativize( path ).toString() );
			}
			catch( IllegalArgumentException x )
			{
				properties.put( index, "file", path.toString() );
			}
			index++;
		}

		Writer writer = new BufferedWriter( new FileWriter( databaseFile ), IoUtil.BUFFER_SIZE );
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
