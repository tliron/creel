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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.creel.util.XmlUtil;

/**
 * Parsed <a href=
 * "http://maven.apache.org/components/ref/3.2.5/maven-repository-metadata/">
 * Maven "maven-metadata.xml"</a>.
 * 
 * @author Tal Liron
 */
public class MetaData
{
	//
	// Construction
	//

	/**
	 * Constructor. Loads the metadata from the URL, optionally validates it
	 * against a signature, and parses it.
	 * 
	 * @param url
	 *        The source URL
	 * @param signature
	 *        The signature or null
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public MetaData( URL url, Signature signature ) throws IOException
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
			throw new CreelException( "Invalid metadata", x );
		}

		// <metadata>
		Element metadata = XmlUtil.getElement( document, "metadata" );
		if( metadata == null )
			throw new CreelException( "Invalid metadata: no <metadata>" );

		groupId = XmlUtil.getFirstElementText( metadata, "groupId" );
		artifactId = XmlUtil.getFirstElementText( metadata, "artifactId" );

		// <versioning>
		Element versioning = XmlUtil.getFirstElement( metadata, "versioning" );
		if( versioning == null )
		{
			release = null;
			return;
		}

		release = XmlUtil.getFirstElementText( versioning, "release" );

		// <versions>, <version>
		for( Element version : new XmlUtil.Elements( XmlUtil.getFirstElement( versioning, "versions" ), "version" ) )
		{
			String versionString = version.getTextContent();
			this.versions.add( versionString );
		}
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
	 * The release version.
	 * 
	 * @return The release version
	 */
	public String getRelease()
	{
		return release;
	}

	/**
	 * The versions.
	 * 
	 * @return The versions
	 */
	public Iterable<String> getVersions()
	{
		return Collections.unmodifiableCollection( versions );
	}

	/**
	 * The release as a module identifier.
	 * 
	 * @param repository
	 *        The repository
	 * @return The release module identifier
	 */
	public MavenModuleIdentifier getReleaseModuleIdentifier( MavenRepository repository )
	{
		return new MavenModuleIdentifier( repository, getGroupId(), getArtifactId(), getRelease() );
	}

	/**
	 * As module identifiers.
	 * 
	 * @param repository
	 *        The repository
	 * @return The module identifiers
	 */
	public Iterable<MavenModuleIdentifier> getModuleIdentifiers( MavenRepository repository )
	{
		List<MavenModuleIdentifier> moduleIdentifiers = new ArrayList<MavenModuleIdentifier>();
		for( String version : getVersions() )
			moduleIdentifiers.add( new MavenModuleIdentifier( repository, getGroupId(), getArtifactId(), version ) );
		Collections.sort( moduleIdentifiers );
		return Collections.unmodifiableCollection( moduleIdentifiers );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;

	private final String release;

	private final Collection<String> versions = new ArrayList<String>();
}
