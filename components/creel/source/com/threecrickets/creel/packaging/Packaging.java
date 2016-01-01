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

package com.threecrickets.creel.packaging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.packaging.internal.Jar;
import com.threecrickets.creel.packaging.internal.Volatiles;

/**
 * Packages are collections of artifacts (see {@link Artifact}). They are
 * defined using special tags in standard JVM resource manifests. Additionally,
 * packages support special install/uninstall hooks for calling arbitrary entry
 * points, allowing for custom behavior. Indeed, a package can include no
 * artifacts, and only implement these hooks.
 * <p>
 * Packages allow you to work around various limitations in repositories such as
 * iBiblio/Maven, in which the smallest deployable unit is a Jar. The package
 * specification allows you to include as many files as you need in a single
 * Jar, greatly simplifying your deployment scheme.
 * <p>
 * Note that two different ways are supported for specifying artifacts: they can
 * specified as files, thus referring to actual zipped entries with the Jar file
 * in which the manifest resides, or that can be specified as general resources,
 * in which case they will be general resource URLs to be loaded by the class
 * loader, and thus they can reside anywhere in the classpath.
 * <p>
 * Also note what "volatile" means in this context: a "volatile" artifact is one
 * that should be installed <i>once and only once</i>. This means that
 * subsequent attempts to install the package, beyond the first, should ignore
 * these artifacts. This is useful for configuration files, example files, and
 * other files that the user should be allow to modify or delete without
 * worrying that they would reappear or be overwritten.
 * <p>
 * Supported manifest tags:
 * <ul>
 * <li><b>Package-Files</b>: a comma separated list of file paths within this
 * Jar.</li>
 * <li><b>Package-Folders</b>: a comma separated list of folder paths within
 * this Jar. Specifies all artifacts under these folders, recursively.</li>
 * <li><b>Package-Resources</b>: a comma separated list of resource paths to be
 * retrieved via the class loader.</li>
 * <li><b>Package-Volatile-Files</b>: all these artifacts will be marked as
 * volatile.</li>
 * <li><b>Package-Volatile-Folders</b>: all artifacts under these paths will be
 * marked as volatile.</li>
 * <li><b>Package-Installer</b>: specifies a class name which has a main() entry
 * point. Simple string arguments can be optionally appended, separated by
 * spaces. The installer will be called when the package is to be installed,
 * <i>after</i> all artifacts have been unpacked. Any thrown exception would
 * cause installation to fail.</li>
 * <li><b>Package-Uninstaller</b>: specifies a class name which has a main()
 * entry point. Simple string arguments can be optionally appended, separated by
 * spaces. The uninstaller will be called when the package is to be uninstalled.
 * </li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class Packaging
{
	//
	// Constants
	//

	public static final String MANIFEST = "META-INF/MANIFEST.MF";

	public static final String PACKAGE_FOLDERS = "Package-Folders";

	public static final String PACKAGE_VOLATILE_FOLDERS = "Package-Volatile-Folders";

	public static final String PACKAGE_FILES = "Package-Files";

	public static final String PACKAGE_VOLATILE_FILES = "Package-Volatile-Files";

	public static final String PACKAGE_RESOURCES = "Package-Resources";

	public static final String PACKAGE_INSTALLER = "Package-Installer";

	public static final String PACKAGE_UNINSTALLER = "Package-Uninstaller";

	//
	// Static operations
	//

	public static Iterable<Package> getPackages( ClassLoader classLoader, File rootDir ) throws IOException
	{
		Collection<Package> packages = new ArrayList<Package>();
		Enumeration<URL> manifestUrls = classLoader.getResources( MANIFEST );
		while( manifestUrls.hasMoreElements() )
		{
			URL manifestUrl = manifestUrls.nextElement();
			Package thePackage = getPackage( manifestUrl, classLoader, rootDir );
			if( thePackage != null )
				packages.add( thePackage );
		}
		return Collections.unmodifiableCollection( packages );
	}

	/**
	 * Creates a package instance by interpreting its manifest.
	 * 
	 * @param manifestUrl
	 *        The manifest URL
	 * @param classLoader
	 *        The class loader
	 * @param rootDir
	 *        The root directory
	 * @return The package or null if not a package manifest
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static Package getPackage( URL manifestUrl, ClassLoader classLoader, File rootDir ) throws IOException
	{
		String installer = null;
		String uninstaller = null;
		Collection<Artifact> artifacts = new ArrayList<Artifact>();

		Jar jar = null;

		try
		{
			InputStream stream = manifestUrl.openStream();
			try
			{
				Attributes manifest = new Manifest( stream ).getMainAttributes();

				// Package installer
				Object packageInstaller = manifest.getValue( PACKAGE_INSTALLER );
				if( packageInstaller != null )
					installer = packageInstaller.toString();

				// Package uninstaller
				Object packageUninstaller = manifest.getValue( PACKAGE_UNINSTALLER );
				if( packageUninstaller != null )
					uninstaller = packageUninstaller.toString();

				Volatiles volatiles = null;

				// Package folders
				Object packageFolders = manifest.getValue( PACKAGE_FOLDERS );
				if( packageFolders != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, rootDir, "Package folders " + packageFolders );
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					for( String packageFolder : packageFolders.toString().split( "," ) )
					{
						String prefix = packageFolder;
						if( !prefix.endsWith( "/" ) )
							prefix += "/";
						int prefixLength = prefix.length();

						URL urlContext = new URL( "jar:" + jar.getUrl() + "!/" + packageFolder );
						for( JarEntry entry : jar.getEntries() )
						{
							String name = entry.getName();
							if( name.startsWith( prefix ) && name.length() > prefixLength )
							{
								URL url = new URL( urlContext, name );
								artifacts.add( new Artifact( new File( rootDir, name.substring( prefixLength ) ), url, volatiles.contains( name ) ) );
							}
						}
					}
				}

				// Package files
				Object packageFiles = manifest.getValue( PACKAGE_FILES );
				if( packageFiles != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, rootDir, "Package files " + packageFiles );
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					for( String packageFile : packageFiles.toString().split( "," ) )
					{
						boolean found = false;
						for( JarEntry entry : jar.getEntries() )
						{
							if( packageFile.equals( entry.getName() ) )
							{
								URL url = new URL( "jar:" + jar.getUrl() + "!/" + packageFile );
								artifacts.add( new Artifact( new File( rootDir, packageFile ), url, volatiles.contains( packageFile ) ) );
								found = true;
								break;
							}
						}
						if( !found )
							throw new RuntimeException( "Package file " + packageFile + " not found in " + jar.getFile() );
					}
				}

				// Package resources
				Object packageResources = manifest.getValue( PACKAGE_RESOURCES );
				if( packageResources != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, rootDir, "Package resources " + packageResources );
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					for( String name : packageResources.toString().split( "," ) )
					{
						URL url = classLoader.getResource( name );
						if( url == null )
							throw new RuntimeException( "Could not find packaged resource " + name + " from " + jar.getFile() );

						artifacts.add( new Artifact( new File( rootDir, name ), url, volatiles.contains( name ) ) );
					}
				}
			}
			finally
			{
				stream.close();
			}
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( "Parsing error in package: " + manifestUrl, x );
		}

		if( ( installer != null ) || ( uninstaller != null ) || !artifacts.isEmpty() )
			return new Package( installer, uninstaller, jar != null ? jar.getFile() : null, artifacts );

		return null;
	}
}
