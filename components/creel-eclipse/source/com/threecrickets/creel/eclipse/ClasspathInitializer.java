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

package com.threecrickets.creel.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Initializes {@link Classpath}.
 * 
 * @author Tal Liron
 */
public class ClasspathInitializer extends ClasspathContainerInitializer
{
	//
	// ClasspathContainerInitializer
	//

	public void initialize( IPath path, IJavaProject project ) throws CoreException
	{
		Classpath classpath = new Classpath( project.getProject() );
		JavaCore.setClasspathContainer( path, new IJavaProject[]
		{
			project
		}, new IClasspathContainer[]
		{
			classpath
		}, null );
	}

	@Override
	public void requestClasspathContainerUpdate( IPath path, IJavaProject project, IClasspathContainer suggestion ) throws CoreException
	{
		JavaCore.setClasspathContainer( path, new IJavaProject[]
		{
			project
		}, new IClasspathContainer[]
		{
			suggestion
		}, null );
	}

	@Override
	public boolean canUpdateClasspathContainer( IPath path, IJavaProject project )
	{
		return true;
	}
}
