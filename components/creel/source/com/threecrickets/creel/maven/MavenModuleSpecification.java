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
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.maven.internal.SpecificationOption;
import com.threecrickets.creel.util.ConfigHelper;
import com.threecrickets.creel.util.GlobUtil;

/**
 * Creel implementation of <a href="https://maven.apache.org/">Maven</a> m2
 * (also known as "ibiblio") module specifications, with various enhancements.
 * <p>
 * Supports <a href=
 * "https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html">version
 * ranges</a>. Note that a Maven version range can in fact contain several
 * ranges, in which case they match via a logical or. For example.
 * "(,1.1),(1.1,)" means that everything except "1.1" will match.
 * <p>
 * Also, you may have a specification with more than one option, which will also
 * match via a logical or, <i>unless</i> the option has a version beginning with
 * a "!". That signifies an exclusion, which will always take precedence. For
 * example, "!1.1" will explicitly reject "1.1", even if "1.1" is matched by
 * other options. This is very useful for easily excluding undesired versions
 * while still specifying broad ranges. Note that the exclusion can itself be a
 * version range.
 * <p>
 * In non-strict mode, will also support wildcards ("*" and "?") in groups and
 * names, and the <a href="http://ant.apache.org/ivy/">Ivy</a>/
 * <a href="http://gradle.org/">Gradle</a>-style "+" suffix for versions. For
 * example, "1.0+" will translate to the "[1.0,)" Maven range.
 * 
 * @author Tal Liron
 */
public class MavenModuleSpecification extends ModuleSpecification
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
	public static MavenModuleSpecification cast( Object object )
	{
		if( object == null )
			throw new NullPointerException();
		if( !( object instanceof MavenModuleSpecification ) )
			throw new IncompatiblePlatformException( "mvn", object );
		return (MavenModuleSpecification) object;
	}

	//
	// Construction
	//

	/**
	 * Constructor for a single option.
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
	public MavenModuleSpecification( String group, String name, String version, boolean strict )
	{
		this( Collections.singleton( new SpecificationOption( group, name, version, strict ) ), strict );
	}

	/**
	 * Constructor.
	 * 
	 * @param options
	 *        The options
	 * @param strict
	 *        Whether we are in strict Maven mode
	 */
	public MavenModuleSpecification( Iterable<SpecificationOption> options, boolean strict )
	{
		for( SpecificationOption option : options )
			this.options.add( option );
		this.strict = strict;
	}

	/**
	 * Config constructor.
	 * 
	 * @param config
	 *        The config
	 */
	public MavenModuleSpecification( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		String group = configHelper.getString( "group" );
		String name = configHelper.getString( "name" );
		if( ( group == null ) || ( name == null ) )
			throw new RuntimeException();
		String version = configHelper.getString( "version" );
		strict = configHelper.getBoolean( "strict", false );
		options.add( new SpecificationOption( group, name, version, strict ) );
	}

	//
	// Attributes
	//

	/**
	 * The options.
	 * 
	 * @return The options
	 */
	public Iterable<SpecificationOption> getOptions()
	{
		return Collections.unmodifiableCollection( options );
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

	//
	// Operations
	//

	/**
	 * Checks if this module specification has an option that matches the
	 * requirements. The requirements may include glob wildcards ("*" and "?").
	 * 
	 * @param group
	 *        The group requirement or null to match all
	 * @param name
	 *        The name requirement or null to match all
	 * @param version
	 *        The version requirement or null to match all
	 * @return True if matches
	 */
	public boolean is( String group, String name, String version )
	{
		Pattern groupPattern = group != null ? GlobUtil.compile( group ) : null;
		Pattern namePattern = name != null ? GlobUtil.compile( name ) : null;
		Pattern versionPattern = version != null ? GlobUtil.compile( version ) : null;
		for( SpecificationOption option : getOptions() )
			if( option.is( groupPattern, namePattern, versionPattern ) )
				return true;
		return false;
	}

	/**
	 * Rewrites the first option that matches the requirements. The requirements
	 * may include glob wildcards ("*" and "?").
	 * 
	 * @param group
	 *        The group requirement or null to match all
	 * @param name
	 *        The name requirement or null to match all
	 * @param version
	 *        The version requirement or null to match all
	 * @param newGroup
	 *        The new group
	 * @param newName
	 *        The new name
	 * @return True if rewritten
	 */
	public boolean rewrite( String group, String name, String version, String newGroup, String newName )
	{
		Pattern groupPattern = group != null ? GlobUtil.compile( group ) : null;
		Pattern namePattern = name != null ? GlobUtil.compile( name ) : null;
		Pattern versionPattern = version != null ? GlobUtil.compile( version ) : null;
		for( SpecificationOption option : getOptions() )
			if( option.is( groupPattern, namePattern, versionPattern ) )
			{
				option.rewrite( newGroup, newName, null );
				return true;
			}
		return false;
	}

	/**
	 * Rewrites the version of the first option that matches the requirements.
	 * The requirements may include glob wildcards ("*" and "?").
	 * 
	 * @param group
	 *        The group requirement or null to match all
	 * @param name
	 *        The name requirement or null to match all
	 * @param version
	 *        The version requirement or null to match all
	 * @param newVersion
	 *        The new version
	 * @return True if rewritten
	 */
	public boolean rewriteVersion( String group, String name, String version, String newVersion )
	{
		Pattern groupPattern = group != null ? GlobUtil.compile( group ) : null;
		Pattern namePattern = name != null ? GlobUtil.compile( name ) : null;
		Pattern versionPattern = version != null ? GlobUtil.compile( version ) : null;
		for( SpecificationOption option : getOptions() )
			if( option.is( groupPattern, namePattern, versionPattern ) )
			{
				option.rewrite( null, null, newVersion );
				return true;
			}
		return false;
	}

	//
	// ModuleSpecification
	//

	public boolean allowsModuleIdentifier( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );

		boolean allowed = false;

		for( SpecificationOption option : getOptions() )
		{
			if( option.matches( mavenModuleIdentifier ) )
			{
				if( option.isExclude() )
					return false;

				// Logical or: takes just one matched option to be allowed
				allowed = true;

				// No need to continue with other options... unless it's an
				// exclusion
				if( !option.isExclude() )
					break;
			}
		}

		return allowed;
	}

	//
	// Cloneable
	//

	public MavenModuleSpecification clone()
	{
		return new MavenModuleSpecification( getOptions(), isStrict() );
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( !super.equals( object ) )
			return false;
		MavenModuleSpecification mavenModuleSpecification = (MavenModuleSpecification) object;

		for( Iterator<SpecificationOption> i1 = getOptions().iterator(), i2 = mavenModuleSpecification.getOptions().iterator(); i1.hasNext() || i2.hasNext(); )
		{
			if( i1.hasNext() != i2.hasNext() )
				return false;
			SpecificationOption o1 = i1.next(), o2 = i2.next();
			if( !o1.equals( o2 ) )
				return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), options, isStrict() );
	}

	@Override
	public String toString()
	{
		String r = "mvn:{";
		for( Iterator<SpecificationOption> i = getOptions().iterator(); i.hasNext(); )
		{
			SpecificationOption option = i.next();
			r += option;
			if( i.hasNext() )
				r += "|";
		}
		r += "}";
		return r;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Collection<SpecificationOption> options = new ArrayList<SpecificationOption>();

	private final boolean strict;
}
