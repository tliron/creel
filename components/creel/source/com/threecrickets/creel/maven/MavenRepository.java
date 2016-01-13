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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Command;
import com.threecrickets.creel.Module;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;
import com.threecrickets.creel.Repository;
import com.threecrickets.creel.Rule;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.CreelException;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.exception.InvalidArtifactException;
import com.threecrickets.creel.maven.internal.MetaData;
import com.threecrickets.creel.maven.internal.POM;
import com.threecrickets.creel.maven.internal.Signature;
import com.threecrickets.creel.maven.internal.SpecificationOption;
import com.threecrickets.creel.util.ConfigHelper;
import com.threecrickets.creel.util.IoUtil;

/**
 * Creel implementation of <a href="https://maven.apache.org/">Maven</a> m2
 * (also known as "ibiblio") repositories.
 * <p>
 * Supports reading the repository URL structure, retrieving and parsing
 * "pom.xml" and "maven-metadata.xml" data, interpreting module identifiers
 * (group/name/version), applying version ranges, downloading ".jar" files, and
 * validating against signatures in ".sha1" or ".md5" files.
 * <p>
 * Supports the following rules:
 * <ul>
 * <li><b>exclude</b>: Excludes modules from installation. Note that their
 * dependencies will be excluded from identification, but can still be pulled in
 * by other modules. To match, set "group", "name", and optionally "version".
 * You can use globs and version ranges.</li>
 * <li><b>excludeDependencies</b>: Excludes modules' dependencies from
 * installation. (In Ivy these are called "transient" dependencies.) To match,
 * set "group", "name", and optionally "version". You can use globs and version
 * ranges.</li>
 * <li><b>rewrite</b>: Rewrites module specifications. To match, set "group",
 * "name", and optionally "version". You can use globs and version ranges. Set
 * either or both of "newGroup" and "newName" to the new value.</li>
 * <li><b>rewriteVersion</b>: Rewrites module versions. To match, set "group",
 * "name", and optionally "version". You can use globs and version ranges. Set
 * "newVersion" to the new value.</li>
 * <li><b>repositories</b>: Look for modules only in specific repositories. By
 * default, all repositories will be used for all modules. Set "all" to false
 * for repositories that you don't want used this way, in which case you will
 * need to use the "repositories" rule to use them with specific modules. To
 * match, set "group", "name", and optionally "version". You can use globs and
 * version ranges. Set "repositories" to a comma-separated list of repository
 * IDs.</li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class MavenRepository extends Repository
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
	public static MavenRepository cast( Object object )
	{
		if( object == null )
			throw new NullPointerException();
		if( !( object instanceof MavenRepository ) )
			throw new IncompatiblePlatformException( "mvn", object );
		return (MavenRepository) object;
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        The repository ID (should be unique in the engine)
	 * @param all
	 *        Whether the engine should attempt to identify all modules in this
	 *        repository
	 * @param url
	 *        The root URL
	 * @param checkSignatures
	 *        Whether we should check signatures for all downloaded files
	 * @param allowMd5
	 *        Whether we should allow for MD5 signatures (considered less
	 *        secure) if SHA-1 signatures are not available
	 */
	public MavenRepository( String id, boolean all, URL url, boolean checkSignatures, boolean allowMd5 )
	{
		super( id, all );
		this.url = url;
		this.checkSignatures = checkSignatures;
		this.allowMd5 = allowMd5;
	}

	/**
	 * Config constructor.
	 * 
	 * @param config
	 *        The config
	 */
	public MavenRepository( Map<String, ?> config )
	{
		super( config );
		ConfigHelper configHelper = new ConfigHelper( config );
		try
		{
			url = new URL( configHelper.getString( "url" ) );
		}
		catch( MalformedURLException x )
		{
			throw new CreelException( x );
		}
		checkSignatures = configHelper.getBoolean( "checkSignatures", true );
		allowMd5 = configHelper.getBoolean( "allowMd5" );
	}

	//
	// Attributes
	//

	/**
	 * The root URL.
	 * 
	 * @return The root URL
	 */
	public URL getUrl()
	{
		return url;
	}

	/**
	 * Whether we should check signatures for all downloaded files
	 * 
	 * @return True to check signatures
	 */
	public boolean isCheckSignatures()
	{
		return checkSignatures;
	}

	/**
	 * Whether we should allow for MD5 signatures (considered less secure) if
	 * SHA-1 signatures are not available
	 * 
	 * @return True to allow MD5
	 */
	public boolean isAllowMd5()
	{
		return allowMd5;
	}

	/**
	 * Gets a local file representing a module artifact in the repository.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param extension
	 *        The file extension
	 * @param classifier
	 *        The classifier or null
	 * @param rootDir
	 *        The root directory
	 * @param flat
	 *        Whether we should use a flat file structure under the root
	 *        directory (no sub-directories)
	 * @return The file
	 */
	public File getFile( MavenModuleIdentifier moduleIdentifier, String extension, String classifier, File rootDir, boolean flat )
	{
		File file;
		try
		{
			file = rootDir.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not access directory: " + rootDir );
		}

		if( flat )
		{
			StringBuilder s = new StringBuilder();
			s.append( moduleIdentifier.getGroup() );
			s.append( '-' );
			s.append( moduleIdentifier.getName() );
			s.append( '-' );
			s.append( moduleIdentifier.getVersion() );
			if( classifier != null )
			{
				s.append( '-' );
				s.append( classifier );
			}
			s.append( '.' );
			s.append( extension );
			file = new File( file, s.toString() );
		}
		else
		{
			file = new File( file, moduleIdentifier.getGroup() );
			file = new File( file, moduleIdentifier.getName() );
			file = new File( file, moduleIdentifier.getVersion() );
			if( classifier != null )
				file = new File( file, moduleIdentifier.getName() + '-' + classifier + '.' + extension );
			else
				file = new File( file, moduleIdentifier.getName() + '.' + extension );
		}
		try
		{
			return file.getCanonicalFile();
		}
		catch( IOException x )
		{
			throw new CreelException( "Could not access file: " + file );
		}
	}

	/**
	 * Gets a URL for a module in the repository.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param extension
	 *        The file extension
	 * @param classifier
	 *        The classifier or null
	 * @return The URL
	 */
	public URL getUrl( MavenModuleIdentifier moduleIdentifier, String extension, String classifier )
	{
		StringBuilder url = new StringBuilder( getUrl().toString() );

		for( String part : moduleIdentifier.getGroup().split( "\\." ) )
		{
			url.append( '/' );
			url.append( part );
		}

		url.append( '/' );
		url.append( moduleIdentifier.getName() );
		url.append( '/' );
		url.append( moduleIdentifier.getVersion() );
		url.append( '/' );
		url.append( moduleIdentifier.getName() );
		url.append( '-' );
		url.append( moduleIdentifier.getVersion() );
		if( classifier != null )
		{
			url.append( '-' );
			url.append( classifier );
		}
		url.append( '.' );
		url.append( extension );

		try
		{
			return new URL( url.toString() ).toURI().normalize().toURL();
		}
		catch( MalformedURLException x )
		{
			throw new CreelException( x );
		}
		catch( URISyntaxException x )
		{
			throw new CreelException( x );
		}
	}

	/**
	 * Gets the URL for a "maven-metadata.xml" in the repository.
	 * 
	 * @param group
	 *        The group
	 * @param name
	 *        The name
	 * @return The URL
	 */
	public URL getMetaDataUrl( String group, String name )
	{
		StringBuilder url = new StringBuilder( getUrl().toString() );

		for( String part : group.split( "\\." ) )
		{
			url.append( '/' );
			url.append( part );
		}

		url.append( '/' );
		url.append( name );
		url.append( "/maven-metadata.xml" );

		try
		{
			return new URL( url.toString() ).toURI().normalize().toURL();
		}
		catch( MalformedURLException x )
		{
			throw new CreelException( x );
		}
		catch( URISyntaxException x )
		{
			throw new CreelException( x );
		}
	}

	/**
	 * Loads a module's "pom.xml", validates it against its signature, and
	 * parses it.
	 * 
	 * @param moduleIdentifier
	 *        The module identifier
	 * @param notifier
	 *        The notifier or null
	 * @return The parsed POM
	 */
	public POM getPom( MavenModuleIdentifier moduleIdentifier, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		// TODO: cache POMs
		URL url = getUrl( moduleIdentifier, "pom", null );
		try
		{
			Signature signature = isCheckSignatures() ? new Signature( url, isAllowMd5() ) : null;
			POM pom = new POM( url, signature );
			if( !moduleIdentifier.equals( pom.getModuleIdentifier( this ) ) )
			{
				notifier.error( "Invalid POM: " + url );
				return null;
			}
			return pom;
		}
		catch( FileNotFoundException x )
		{
			notifier.debug( "No POM: " + url );
			return null;
		}
		catch( InvalidArtifactException x )
		{
			notifier.error( "Invalid signature for POM: " + url );
			return null;
		}
		catch( IOException x )
		{
			throw new CreelException( x );
		}
	}

	/**
	 * Loads a "maven-metadata.xml", validates it against its signature, and
	 * parses it.
	 * 
	 * @param group
	 *        The group
	 * @param name
	 *        The name
	 * @param notifier
	 *        The notifier or null
	 * @return The parsed metadata
	 */
	public MetaData getMetaData( String group, String name, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		// TODO: cache metadata!
		URL url = getMetaDataUrl( group, name );
		try
		{
			Signature signature = isCheckSignatures() ? new Signature( url, isAllowMd5() ) : null;
			MetaData metadata = new MetaData( url, signature );
			if( !group.equals( metadata.getGroupId() ) || !name.equals( metadata.getArtifactId() ) )
			{
				notifier.error( "Invalid metadata: " + url );
				return null;
			}
			return metadata;
		}
		catch( FileNotFoundException x )
		{
			notifier.debug( "No metadata: " + url );
			return null;
		}
		catch( InvalidArtifactException x )
		{
			notifier.error( "Invalid signature for metadata: " + url );
			return null;
		}
		catch( IOException x )
		{
			throw new CreelException( x );
		}
	}

	//
	// Repository
	//

	public Iterable<ModuleIdentifier> getAllowedModuleIdentifiers( ModuleSpecification moduleSpecification, Notifier notifier )
	{
		MavenModuleSpecification mavenModuleSpecification = MavenModuleSpecification.cast( moduleSpecification );
		if( notifier == null )
			notifier = new Notifier();

		Set<ModuleIdentifier> potentialModuleIdentifiers = new LinkedHashSet<ModuleIdentifier>();
		for( SpecificationOption option : mavenModuleSpecification.getOptions() )
		{
			MavenModuleIdentifier trivialModuleIdentifier = option.toTrivialModuleIdentifier( this );
			if( trivialModuleIdentifier != null )
			{
				// When the version is trivial, we can skip the metadata and
				// just check directly against the POM
				if( hasModule( trivialModuleIdentifier ) )
					potentialModuleIdentifiers.add( trivialModuleIdentifier );
			}
			else
			{
				MetaData metadata = getMetaData( option.getGroup(), option.getName(), notifier );
				if( metadata != null )
					for( MavenModuleIdentifier moduleIdentifier : metadata.getModuleIdentifiers( this ) )
						potentialModuleIdentifiers.add( moduleIdentifier );
			}
		}

		return moduleSpecification.filterAllowedModuleIdentifiers( potentialModuleIdentifiers );
	}

	public boolean hasModule( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );
		URL url = getUrl( mavenModuleIdentifier, "pom", null );
		return IoUtil.exists( url );
	}

	public Module getModule( ModuleIdentifier moduleIdentifier, Notifier notifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );
		if( notifier == null )
			notifier = new Notifier();

		POM pom = getPom( mavenModuleIdentifier, notifier );
		if( pom == null )
			return null;

		Module module = new Module( false, moduleIdentifier, null );
		for( MavenModuleSpecification moduleSpecification : pom.getDependencyModuleSpecifications() )
		{
			Module dependencyModule = new Module( false, null, moduleSpecification );
			dependencyModule.addSupplicant( module );
			module.addDependency( dependencyModule );
		}
		return module;
	}

	public void validateArtifact( ModuleIdentifier moduleIdentifier, Artifact artifact, Notifier notifier )
	{
		MavenModuleIdentifier.cast( moduleIdentifier );

		if( !isCheckSignatures() )
			return;

		if( notifier == null )
			notifier = new Notifier();

		try
		{
			Signature signature = new Signature( artifact.getSourceUrl(), allowMd5 );
			if( !signature.validate( artifact.getFile() ) )
			{
				notifier.error( "Invalid, so deleting " + artifact.getFile() );
				artifact.getFile().delete();
				// IoUtil.deleteWithParentDirectories( artifact.getFile(), root
				// );
				throw new InvalidArtifactException( artifact );
			}
		}
		catch( IOException x )
		{
			throw new CreelException( x );
		}
	}

	@Override
	public Runnable validateArtifactTask( final ModuleIdentifier moduleIdentifier, final Artifact artifact, final Notifier notifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );

		if( !isCheckSignatures() )
			return null;

		return super.validateArtifactTask( mavenModuleIdentifier, artifact, notifier );
	}

	public Command applyRule( Module module, Rule rule, Notifier notifier )
	{
		if( !"mvn".equals( rule.getPlatform() ) )
			return null;

		if( notifier == null )
			notifier = new Notifier();

		if( "exclude".equals( rule.getType() ) )
		{
			MavenModuleSpecification moduleSpecification = MavenModuleSpecification.cast( module.getSpecification() );
			String group = rule.get( "group" );
			String name = rule.get( "name" );
			String version = rule.get( "version" );
			if( moduleSpecification.is( group, name, version ) )
				return new Command( "excludeModule" );
			else
				return new Command( "handled" );
		}
		else if( "excludeDependencies".equals( rule.getType() ) )
		{
			MavenModuleSpecification moduleSpecification = MavenModuleSpecification.cast( module.getSpecification() );
			String group = rule.get( "group" );
			String name = rule.get( "name" );
			String version = rule.get( "version" );
			if( moduleSpecification.is( group, name, version ) )
				return new Command( "excludeDependencies" );
			else
				return new Command( "handled" );
		}
		else if( "rewrite".equals( rule.getType() ) )
		{
			MavenModuleSpecification moduleSpecification = MavenModuleSpecification.cast( module.getSpecification() );
			String group = rule.get( "group" );
			String name = rule.get( "name" );
			String version = rule.get( "version" );
			String newGroup = rule.get( "newGroup" );
			String newName = rule.get( "newName" );
			if( moduleSpecification.rewrite( group, name, version, newGroup, newName ) )
				notifier.info( "Rewrote " + module.getSpecification() );
			return new Command( "handled" );
		}
		else if( "rewriteVersion".equals( rule.getType() ) )
		{
			MavenModuleSpecification moduleSpecification = MavenModuleSpecification.cast( module.getSpecification() );
			String group = rule.get( "group" );
			String name = rule.get( "name" );
			String version = rule.get( "version" );
			String newVersion = rule.get( "newVersion" );
			if( moduleSpecification.rewriteVersion( group, name, version, newVersion ) )
				notifier.info( "Rewrote version of " + module.getSpecification() );
			return new Command( "handled" );
		}
		else if( "repositories".equals( rule.getType() ) )
		{
			MavenModuleSpecification moduleSpecification = MavenModuleSpecification.cast( module.getSpecification() );
			String group = rule.get( "group" );
			String name = rule.get( "name" );
			String version = rule.get( "version" );
			String repositories = rule.get( "repositories" );
			if( moduleSpecification.is( group, name, version ) )
			{
				Command command = new Command( "setRepositories" );
				command.put( "repositories", Arrays.asList( repositories.split( "," ) ) );
				return command;
			}
			else
				return new Command( "handled" );
		}

		return null;
	}

	//
	// Cloneable
	//

	public MavenRepository clone()
	{
		return new MavenRepository( getId(), isAll(), getUrl(), isCheckSignatures(), isAllowMd5() );
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( !super.equals( object ) )
			return false;
		MavenRepository mavenRepository = (MavenRepository) object;
		return getUrl().equals( mavenRepository.getUrl() ) && ( isCheckSignatures() == mavenRepository.isCheckSignatures() ) && ( isAllowMd5() == mavenRepository.isAllowMd5() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), getUrl(), isCheckSignatures(), isAllowMd5() );
	}

	@Override
	public String toString()
	{
		return "mvn: id=" + getId() + ", url=maven:" + getUrl() + ", checkSignatures=" + isCheckSignatures() + ", allowMd5=" + isAllowMd5();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final boolean checkSignatures;

	private final boolean allowMd5;
}
