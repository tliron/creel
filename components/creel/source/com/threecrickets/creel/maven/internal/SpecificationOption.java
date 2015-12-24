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

package com.threecrickets.creel.maven.internal;

import java.util.Objects;
import java.util.regex.Pattern;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.creel.util.GlobUtil;

/**
 * @author Tal Liron
 */
public class SpecificationOption
{
	//
	// Construction
	//

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

	public String getGroup()
	{
		return group;
	}

	public String getName()
	{
		return name;
	}

	public String getVersion()
	{
		return version;
	}

	public boolean isStrict()
	{
		return strict;
	}

	public boolean isExclude()
	{
		return exclude;
	}

	public Pattern getGroupPattern()
	{
		if( groupPattern == null )
			groupPattern = GlobUtil.toPattern( getGroup() );
		return groupPattern;
	}

	public Pattern getNamePattern()
	{
		if( namePattern == null )
			namePattern = GlobUtil.toPattern( getName() );
		return namePattern;
	}

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

	public boolean matches( MavenModuleIdentifier moduleIdentifier )
	{
		return matches( moduleIdentifier.getGroup(), moduleIdentifier.getName(), moduleIdentifier.getParsedVersion() );
	}

	public boolean matches( String group, String name, Version version )
	{
		if( !getGroupPattern().matcher( group ).matches() )
			return false;
		if( !getNamePattern().matcher( name ).matches() )
			return false;
		if( !getParsedVersionSpecfication().allows( version ) )
			return false;
		return true;
	}

	public boolean inPatterns( Pattern group, Pattern name, Pattern version )
	{
		if( ( group != null ) && !group.matcher( getGroup() ).matches() )
			return false;
		if( ( name != null ) && !name.matcher( getName() ).matches() )
			return false;
		if( ( version != null ) && !name.matcher( getVersion() ).matches() )
			return false;
		return true;
	}

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

	public MavenModuleIdentifier toModuleIdentifier( MavenRepository repository )
	{
		if( getParsedVersionSpecfication().isTrivial() )
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
		return getGroup().equals( option.getGroup() ) && getName().equals( option.getName() ) && getVersion().equals( option.getVersion() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getGroup(), getName(), getVersion() );
	}

	@Override
	public String toString()
	{
		return getGroup() + ":" + getName() + ":" + getVersion();
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
