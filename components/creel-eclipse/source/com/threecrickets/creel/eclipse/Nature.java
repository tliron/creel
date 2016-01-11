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

import java.util.HashMap;
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

		Map<String, String> arguments = new HashMap<String, String>();
		if( Builder.hasDefaultConfigurationFile( getProject() ) )
			arguments.put( Builder.CONFIGURATION_ARGUMENT, "creel.properties" );
		arguments.put( Builder.DEFAULT_ARGUMENT, "" );
		arguments.put( Builder.LIBRARY_ARGUMENT, "libraries/jars" );
		arguments.put( Builder.API_ARGUMENT, "reference/api" );
		arguments.put( Builder.SOURCE_ARGUMENT, "reference/source" );

		EclipseUtil.addBuilder( project, Builder.ID, arguments );
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

	private IProject project;
}
