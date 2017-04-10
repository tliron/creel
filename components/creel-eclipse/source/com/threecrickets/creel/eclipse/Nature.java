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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.threecrickets.creel.eclipse.internal.EclipseUtil;

/**
 * Creel nature. Will configure the project to have a Creel {@link Builder} and,
 * if it's a JDT project, a {@link Classpath}.
 * 
 * @author Tal Liron
 */
public class Nature implements IProjectNature
{
	//
	// Constants
	//

	public static final String ID = Plugin.ID + ".nature";

	//
	// IProjectNature
	//

	public void configure() throws CoreException
	{
		if( project.hasNature( JavaCore.NATURE_ID ) )
		{
			IJavaProject javaProject = JavaCore.create( project );
			EclipseUtil.setClasspathContainer( javaProject, new Classpath( project ) );
		}

		Map<String, String> arguments = Builder.getDefaultArguments( project );
		EclipseUtil.setBuilder( project, Builder.ID, arguments );
	}

	public void deconfigure() throws CoreException
	{
		if( project.hasNature( JavaCore.NATURE_ID ) )
		{
			IJavaProject javaProject = JavaCore.create( project );
			EclipseUtil.removeClasspathContainer( javaProject, Classpath.PATH );
		}

		EclipseUtil.removeBuilder( project, Builder.ID );
	}

	public IProject getProject()
	{
		return project;
	}

	public void setProject( IProject project )
	{
		this.project = project;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile IProject project;
}
