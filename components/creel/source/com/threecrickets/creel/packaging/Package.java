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

package com.threecrickets.creel.packaging;

import java.io.File;
import java.util.Iterator;

import com.threecrickets.creel.Artifact;

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
public class Package implements Iterable<Artifact>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param installer
	 *        Installer command or null
	 * @param uninstaller
	 *        Uninstaller command or null
	 * @param sourceFile
	 *        The source file
	 * @param artifacts
	 *        The artifacts
	 */
	public Package( String installer, String uninstaller, File sourceFile, Iterable<Artifact> artifacts )
	{
		this.installer = installer;
		this.uninstaller = uninstaller;
		this.sourceFile = sourceFile;
		this.artifacts = artifacts;
	}

	//
	// Attributes
	//

	/**
	 * The installer command.
	 * 
	 * @return The installer command or null
	 */
	public String getInstaller()
	{
		return installer;
	}

	/**
	 * The uninstaller command.
	 * 
	 * @return The uninstaller command or null
	 */
	public String getUninstaller()
	{
		return uninstaller;
	}

	/**
	 * The package file (a Jar).
	 * 
	 * @return The file
	 */
	public File getSourceFile()
	{
		return sourceFile;
	}

	//
	// Iterable
	//

	@Override
	public Iterator<Artifact> iterator()
	{
		return artifacts.iterator();
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return "file: " + sourceFile.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String installer;

	private final String uninstaller;

	private final File sourceFile;

	private final Iterable<Artifact> artifacts;
}