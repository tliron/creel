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
import java.util.Iterator;

import com.threecrickets.creel.Artifact;

/**
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