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

package com.threecrickets.creel.eclipse;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.eclipse.internal.Text;

/**
 * Represents the artifacts installed by Creel as a JDT classpath.
 * <p>
 * Path ID: "com.threecrickets.creel.classpath"
 * 
 * @author Tal Liron
 */
public class Classpath implements IClasspathContainer
{
	//
	// Constants
	//

	public final static Path ID = new Path( Plugin.ID + ".classpath" );

	//
	// Construction
	//

	public Classpath( IProject project )
	{
		this.project = project;
	}

	//
	// Attributes
	//

	public boolean has( File file )
	{
		return true;
	}

	//
	// IClasspathContainer
	//

	public int getKind()
	{
		return IClasspathContainer.K_APPLICATION;
	}

	public IClasspathEntry[] getClasspathEntries()
	{
		try
		{
			Engine engine = new Engine();
			engine.getDirectories().set( Builder.toDirectories( Builder.getFolders( project ) ) );
			engine.load();

			Collection<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

			Map<String, Iterable<Artifact>> artifacts = engine.getInstalledArtifactsByModuleIdentifier();
			for( Map.Entry<String, Iterable<Artifact>> entry : artifacts.entrySet() )
			{
				Artifact libraryArtifact = null;
				Artifact apiArtifact = null;
				Artifact sourceArtifact = null;

				for( Artifact artifact : entry.getValue() )
				{
					Artifact.Type type = artifact.getType();
					if( type == Artifact.Type.LIBRARY )
						libraryArtifact = artifact;
					else if( type == Artifact.Type.API )
						apiArtifact = artifact;
					else if( type == Artifact.Type.SOURCE )
						sourceArtifact = artifact;
				}

				if( libraryArtifact == null )
					continue;

				IPath libraryPath = new Path( libraryArtifact.getFile().getPath() );

				IClasspathAttribute[] attributes = null;
				try
				{
					attributes = new IClasspathAttribute[]
					{
						JavaCore.newClasspathAttribute( IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, apiArtifact.getFile().toURI().toURL().toString() )
					};
				}
				catch( MalformedURLException e )
				{
				}

				IPath sourcePath = sourceArtifact != null ? new Path( sourceArtifact.getFile().getPath() ) : null;

				entries.add( JavaCore.newLibraryEntry( libraryPath, sourcePath, null, null, attributes, false ) );
			}

			return entries.toArray( new IClasspathEntry[entries.size()] );
		}
		catch( CoreException x )
		{
			throw new RuntimeException( x );
		}
	}

	public String getDescription()
	{
		return Text.ClasspathName;
	}

	public IPath getPath()
	{
		return ID;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final IProject project;
}
