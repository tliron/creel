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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenModuleSpecification;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.XmlUtil;

/**
 * Parsed <a href="https://maven.apache.org/pom.html">Maven "pom.xml"</a>.
 * 
 * @author Tal Liron
 */
public class POM
{
	//
	// Construction
	//

	/**
	 * Constructor. Loads the POM from the URL, optionally validates it against
	 * a signature, and parses it.
	 * 
	 * @param url
	 *        The source URL
	 * @param signature
	 *        The signature or null
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public POM( URL url, Signature signature ) throws IOException
	{
		Document document;
		try
		{
			byte[] bytes = IoUtil.readBytes( url, null );
			if( ( signature != null ) && !signature.validate( bytes ) )
				throw new InvalidSignatureException();
			String text = new String( bytes, StandardCharsets.UTF_8 );
			document = XmlUtil.parse( text );
		}
		catch( ParserConfigurationException x )
		{
			throw new CreelException( x );
		}
		catch( SAXException x )
		{
			throw new CreelException( "Invalid POM", x );
		}

		// <project>
		Element project = XmlUtil.getElement( document, "project" );
		if( project == null )
			throw new CreelException( "Invalid POM: no <project>" );

		// <properties>
		properties = new Properties( XmlUtil.getFirstElement( project, "properties" ) );

		// <parent>
		Element parent = XmlUtil.getFirstElement( project, "parent" );
		if( parent != null )
		{
			parentGroupId = properties.interpolate( XmlUtil.getFirstElementText( parent, "groupId" ), "project.groupId" );
			parentVersion = properties.interpolate( XmlUtil.getFirstElementText( parent, "version" ), "project.version" );
		}
		else
		{
			parentGroupId = null;
			parentVersion = null;
		}

		groupId = properties.interpolate( XmlUtil.getFirstElementText( project, "groupId" ), "project.groupId" );
		artifactId = properties.interpolate( XmlUtil.getFirstElementText( project, "artifactId" ), "project.artifactId" );
		version = properties.interpolate( XmlUtil.getFirstElementText( project, "version" ), "project.version" );
		name = properties.interpolate( XmlUtil.getFirstElementText( project, "name" ), "project.name" );
		description = properties.interpolate( XmlUtil.getFirstElementText( project, "description" ), "project.description" );

		// <dependencies>, <dependency>
		for( Element dependency : new XmlUtil.Elements( XmlUtil.getFirstElement( project, "dependencies" ), "dependency" ) )
			this.dependencies.add( new Dependency( dependency, properties ) );
	}

	//
	// Attributes
	//

	/**
	 * The properties.
	 * 
	 * @return The properties
	 */
	public Map<String, String> getProperties()
	{
		return Collections.unmodifiableMap( properties );
	}

	/**
	 * The parent group ID.
	 * 
	 * @return The parent group ID
	 */
	public String getParentGroupId()
	{
		return parentGroupId;
	}

	/**
	 * The parent version.
	 * 
	 * @return The parent version
	 */
	public String getParentVersion()
	{
		return parentVersion;
	}

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
	 * The name.
	 * 
	 * @return The name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The description.
	 * 
	 * @return The description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * The dependencies.
	 * 
	 * @return The dependencies
	 */
	public Iterable<Dependency> getDependencies()
	{
		return Collections.unmodifiableCollection( dependencies );
	}

	/**
	 * As a module identifier.
	 * 
	 * @param repository
	 *        The repository
	 * @return The module identifier
	 */
	public MavenModuleIdentifier getModuleIdentifier( MavenRepository repository )
	{
		return new MavenModuleIdentifier( repository, getGroupId() != null ? getGroupId() : getParentGroupId(), getArtifactId(), getVersion() != null ? getVersion() : getParentVersion() );
	}

	/**
	 * The dependencies as module specifications.
	 * 
	 * @return The module specifications
	 */
	public Iterable<MavenModuleSpecification> getDependencyModuleSpecifications()
	{
		Collection<MavenModuleSpecification> moduleSpecifications = new ArrayList<MavenModuleSpecification>();
		for( Dependency dependency : dependencies )
		{
			if( dependency.isOmitted() )
				continue;
			moduleSpecifications.add( new MavenModuleSpecification( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), true ) );
		}
		return Collections.unmodifiableCollection( moduleSpecifications );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Properties properties;

	private final String parentGroupId;

	private final String parentVersion;

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String name;

	private final String description;

	private final Collection<Dependency> dependencies = new ArrayList<Dependency>();
}