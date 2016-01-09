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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Initializes {@link ClasspathContainer}.
 * 
 * @author Tal Liron
 */
public class ClasspathContainerInitializer extends org.eclipse.jdt.core.ClasspathContainerInitializer
{
	//
	// ClasspathContainerInitializer
	//

	public void initialize( IPath path, IJavaProject project ) throws CoreException
	{
		ClasspathContainer container = new ClasspathContainer( project.getProject() );
		JavaCore.setClasspathContainer( path, new IJavaProject[]
		{
			project
		}, new IClasspathContainer[]
		{
			container
		}, null );
	}

	@Override
	public void requestClasspathContainerUpdate( IPath path, IJavaProject project, IClasspathContainer containerSuggestion ) throws CoreException
	{
	}

	@Override
	public boolean canUpdateClasspathContainer( IPath path, IJavaProject project )
	{
		return false;
	}
}
