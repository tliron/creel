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

package com.threecrickets.creel.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Directories;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.exception.IncompatibleIdentifiersException;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.maven.internal.Version;

/**
 * Creel implementation of <a href="https://maven.apache.org/">Maven</a> m2
 * (also known as "ibiblio") module identifiers.
 * <p>
 * Maven identifiers have a group, name, and version.
 * <p>
 * Version comparison (see {@link MavenModuleIdentifier#getParsedVersion()})
 * follows dot notation rules and semantic values of alphanumeric suffixes. For
 * example, "1.0-alpha2" is greater than "1.0-alpha1", but lesser than
 * "1.0-beta1".
 * 
 * @author Tal Liron
 */
public class MavenModuleIdentifier extends ModuleIdentifier
{
	//
	// Static operations
	//

	/**
	 * Casts the object to this class. If it cannot be cast, will throw a
	 * {@link IncompatiblePlatformException}.
	 * 
	 * @param object
	 *        The object
	 * @return The cast object
	 */
	public static MavenModuleIdentifier cast( Object object )
	{
		if( object == null )
			throw new NullPointerException();
		if( !( object instanceof MavenModuleIdentifier ) )
			throw new IncompatiblePlatformException( "mvn", object );
		return (MavenModuleIdentifier) object;
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param repository
	 *        The repository
	 * @param group
	 *        The group
	 * @param name
	 *        The name
	 * @param version
	 *        The version
	 */
	public MavenModuleIdentifier( MavenRepository repository, String group, String name, String version )
	{
		super( repository );
		this.group = group == null ? "" : group.trim();
		this.name = name == null ? "" : name.trim();
		this.version = version == null ? "" : version.trim();
	}

	//
	// Attributes
	//

	/**
	 * The group.
	 * 
	 * @return The group
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * The name.
	 * 
	 * @return The name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The version.
	 * 
	 * @return The version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * The parsed version.
	 * 
	 * @return The parsed version
	 */
	public Version getParsedVersion()
	{
		if( parsedVersion == null )
			parsedVersion = new Version( getVersion() );
		return parsedVersion;
	}

	//
	// ModuleIdentifier
	//

	public Iterable<Artifact> getArtifacts( Directories directories, boolean flat )
	{
		MavenRepository repository = (MavenRepository) getRepository();
		Collection<Artifact> artifacts = new ArrayList<Artifact>();
		if( directories.getLibrary() != null )
			artifacts.add( new Artifact( Artifact.Type.LIBRARY, repository.getFile( this, "jar", null, directories.getLibrary(), flat ), repository.getUrl( this, "jar", null ), false ) );
		if( directories.getApi() != null )
			artifacts.add( new Artifact( Artifact.Type.API, repository.getFile( this, "jar", "javadoc", directories.getApi(), flat ), repository.getUrl( this, "jar", "javadoc" ), false ) );
		if( directories.getSource() != null )
			artifacts.add( new Artifact( Artifact.Type.SOURCE, repository.getFile( this, "jar", "sources", directories.getSource(), flat ), repository.getUrl( this, "jar", "sources" ), false ) );
		return Collections.unmodifiableCollection( artifacts );
	}

	//
	// Comparable
	//

	public int compareTo( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = cast( moduleIdentifier );
		if( getGroup().equals( mavenModuleIdentifier.getGroup() ) && getName().equals( mavenModuleIdentifier.getName() ) )
			return getParsedVersion().compareTo( mavenModuleIdentifier.getParsedVersion() );
		throw new IncompatibleIdentifiersException( this, moduleIdentifier );
	}

	//
	// Cloneable
	//

	public MavenModuleIdentifier clone()
	{
		return new MavenModuleIdentifier( (MavenRepository) getRepository(), getGroup(), getName(), getVersion() );
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( !super.equals( object ) )
			return false;
		MavenModuleIdentifier mavenModuleIdentifier = (MavenModuleIdentifier) object;
		return getGroup().equals( mavenModuleIdentifier.getGroup() ) && getName().equals( mavenModuleIdentifier.getName() ) && getVersion().equals( mavenModuleIdentifier.getVersion() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), getGroup(), getName(), getVersion() );
	}

	@Override
	public String toString()
	{
		return "mvn:" + getGroup() + ":" + getName() + ":" + getVersion();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String group;

	private final String name;

	private final String version;

	private volatile Version parsedVersion;
}
