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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Element;

import com.threecrickets.creel.util.XmlUtil;

/**
 * Parsed Maven pom.xml dependency.
 * 
 * @author Tal Liron
 */
public class Dependency
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param element
	 *        The XML element
	 * @param properties
	 *        The parsed Maven properties
	 */
	public Dependency( Element element, Properties properties )
	{
		groupId = properties.interpolate( XmlUtil.getFirstElementText( element, "groupId" ) );
		artifactId = properties.interpolate( XmlUtil.getFirstElementText( element, "artifactId" ) );
		version = properties.interpolate( XmlUtil.getFirstElementText( element, "version" ) );
		type = properties.interpolate( XmlUtil.getFirstElementText( element, "type" ) );
		scope = properties.interpolate( XmlUtil.getFirstElementText( element, "scope" ) );
		optional = "true".equals( properties.interpolate( XmlUtil.getFirstElementText( element, "optional" ) ) );

		// <exclusions>, <exclusion>
		for( Element exclusion : new XmlUtil.Elements( XmlUtil.getFirstElement( element, "exclusions" ), "exclusion" ) )
			this.exclusions.add( new Exclusion( exclusion, properties ) );
	}

	//
	// Attributes
	//

	/**
	 * The group ID.
	 * 
	 * @return The group ID
	 */
	public String getGroupId()
	{
		return groupId;
	}

	/**
	 * The artifact ID.
	 * 
	 * @return The artifact ID
	 */
	public String getArtifactId()
	{
		return artifactId;
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
	 * The type.
	 * 
	 * @return The type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * The scope.
	 * 
	 * @return The scope
	 */
	public String getScope()
	{
		return scope;
	}

	/**
	 * Whether the dependency is optional.
	 * 
	 * @return True if optional
	 */
	public boolean isOptional()
	{
		return optional;
	}

	/**
	 * The exclusions.
	 * 
	 * @return The exclusions
	 */
	public Iterable<Exclusion> getExclusions()
	{
		return Collections.unmodifiableCollection( exclusions );
	}

	/**
	 * True if should be omitted: optional, or in the "provided", "system", or
	 * "test" scopes.
	 * 
	 * @return True if omitted
	 */
	public boolean isOmitted()
	{
		return optional || "provided".equals( scope ) || "system".equals( scope ) || "test".equals( scope );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String type;

	private final String scope;

	private final boolean optional;

	private final Collection<Exclusion> exclusions = new ArrayList<Exclusion>();
}