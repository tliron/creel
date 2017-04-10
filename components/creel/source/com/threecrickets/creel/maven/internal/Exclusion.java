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

import org.w3c.dom.Element;

import com.threecrickets.creel.util.XmlUtil;

/**
 * Parsed Maven pom.xml exclusion.
 * 
 * @author Tal Liron
 */
public class Exclusion
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
	public Exclusion( Element element, Properties properties )
	{
		groupId = properties.interpolate( XmlUtil.getFirstElementText( element, "groupId" ) );
		artifactId = properties.interpolate( XmlUtil.getFirstElementText( element, "artifactId" ) );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;
}