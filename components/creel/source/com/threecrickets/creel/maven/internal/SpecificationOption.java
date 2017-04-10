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

package com.threecrickets.creel.maven.internal;

import java.util.Objects;
import java.util.regex.Pattern;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.creel.util.GlobUtil;

/**
 * Maven module specification option.
 * 
 * @author Tal Liron
 */
public class SpecificationOption
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param group
	 *        The group specification (glob pattern in non-strict mode)
	 * @param name
	 *        The name specification (glob pattern in non-strict mode)
	 * @param version
	 *        The version specification (exclusions start with "!" prefix)
	 * @param strict
	 *        Whether we are in strict Maven mode
	 */
	public SpecificationOption( String group, String name, String version, boolean strict )
	{
		group = group == null ? "" : group.trim();
		name = name == null ? "" : name.trim();
		version = version == null ? "" : version.trim();
		this.group = group.isEmpty() ? "*" : group;
		this.name = name.isEmpty() ? "*" : name;
		this.version = version;
		this.strict = strict;
		exclude = !version.isEmpty() && ( version.charAt( 0 ) == '!' );
	}

	//
	// Attributes
	//

	/**
	 * The group specification (glob pattern in non-strict mode).
	 * 
	 * @return The group specification
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * The name specification (glob pattern in non-strict mode).
	 * 
	 * @return The name specification
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The version specification.
	 * 
	 * @return The version specification
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Whether we are in strict Maven mode.
	 * 
	 * @return True if strict
	 */
	public boolean isStrict()
	{
		return strict;
	}

	/**
	 * Whether this is an exclusion option.
	 * 
	 * @return True to exclude
	 */
	public boolean isExclude()
	{
		return exclude;
	}

	/**
	 * The compiled group specification pattern.
	 * 
	 * @return The group specification pattern
	 */
	public Pattern getGroupPattern()
	{
		if( groupPattern == null )
			groupPattern = GlobUtil.compile( getGroup() );
		return groupPattern;
	}

	/**
	 * The compiled name specification pattern.
	 * 
	 * @return The name specification pattern
	 */
	public Pattern getNamePattern()
	{
		if( namePattern == null )
			namePattern = GlobUtil.compile( getName() );
		return namePattern;
	}

	/**
	 * The parsed version specification.
	 * 
	 * @return The version specification
	 */
	public VersionSpecification getParsedVersionSpecfication()
	{
		if( parsedVersionSpecification == null )
		{
			String version = isExclude() ? getVersion().substring( 1 ) : getVersion();
			parsedVersionSpecification = new VersionSpecification( version, isStrict() );
		}
		return parsedVersionSpecification;
	}

	//
	// Operations
	//

	/**
	 * Checks whether a module identifier matches this option.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @return True if matches
	 */
	public boolean matches( MavenModuleIdentifier moduleIdentifier )
	{
		return matches( moduleIdentifier.getGroup(), moduleIdentifier.getName(), moduleIdentifier.getParsedVersion() );
	}

	/**
	 * Checks if the values match this option.
	 * 
	 * @param group
	 *        The group
	 * @param name
	 *        The name
	 * @param version
	 *        The version
	 * @return True if matches
	 */
	public boolean matches( String group, String name, Version version )
	{
		if( isStrict() )
		{
			if( !getGroup().equals( group ) )
				return false;
			if( !getName().equals( name ) )
				return false;
		}
		else
		{
			if( !getGroupPattern().matcher( group ).matches() )
				return false;
			if( !getNamePattern().matcher( name ).matches() )
				return false;
		}
		if( !getParsedVersionSpecfication().allows( version ) )
			return false;
		return true;
	}

	/**
	 * Checks if this option matches the patterns.
	 * 
	 * @param groupPattern
	 *        The group pattern
	 * @param namePattern
	 *        The name pattern
	 * @param versionPattern
	 *        The version pattern
	 * @return True if matches
	 */
	public boolean is( Pattern groupPattern, Pattern namePattern, Pattern versionPattern )
	{
		if( ( groupPattern != null ) && !groupPattern.matcher( getGroup() ).matches() )
			return false;
		if( ( namePattern != null ) && !namePattern.matcher( getName() ).matches() )
			return false;
		if( ( versionPattern != null ) && !namePattern.matcher( getVersion() ).matches() )
			return false;
		return true;
	}

	/**
	 * Rewrites the option.
	 * 
	 * @param group
	 *        The new group or null
	 * @param name
	 *        The new name or null
	 * @param version
	 *        The new version or null
	 */
	public void rewrite( String group, String name, String version )
	{
		if( group != null )
			this.group = group;
		if( name != null )
			this.name = name;
		if( version != null )
		{
			// TODO: how to deal with exclusions?
			// TODO: should these by synchronized to change together?
			this.version = version;
			this.parsedVersionSpecification = null;
		}
	}

	/**
	 * If the version is trivial (no ranges), transforms the option into a
	 * module identifier.
	 * 
	 * @param repository
	 *        The repository
	 * @return The module identifier
	 */
	public MavenModuleIdentifier toTrivialModuleIdentifier( MavenRepository repository )
	{
		if( getParsedVersionSpecfication().isTrivial() && !GlobUtil.hasWildcards( getGroup() ) && !GlobUtil.hasWildcards( getName() ) )
			return new MavenModuleIdentifier( repository, getGroup(), getName(), getVersion() );
		return null;
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		SpecificationOption option = (SpecificationOption) object;
		return getGroup().equals( option.getGroup() ) && getName().equals( option.getName() ) && getVersion().equals( option.getVersion() ) && ( isStrict() == option.isStrict() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getGroup(), getName(), getVersion() );
	}

	@Override
	public String toString()
	{
		return getGroup() + ":" + getName() + ( !getVersion().isEmpty() ? ":" + getVersion() : "" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final boolean strict;

	private final boolean exclude;

	private volatile String group;

	private volatile String name;

	private volatile String version;

	private volatile Pattern groupPattern;

	private volatile Pattern namePattern;

	private volatile VersionSpecification parsedVersionSpecification;
}
