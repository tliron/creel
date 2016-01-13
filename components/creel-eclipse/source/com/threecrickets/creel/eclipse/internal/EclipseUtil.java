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

package com.threecrickets.creel.eclipse.internal;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;

import com.threecrickets.creel.eclipse.Classpath;

/**
 * Utilities for Eclipse.
 * 
 * @author Tal Liron
 */
public abstract class EclipseUtil
{
	// Files

	public static IFile getInterpolatedFile( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		return project.getFile( name );
	}

	public static IContainer getInterpolatedFolder( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		// Note: project.getFolder cannot get the root! (?)
		return name.isEmpty() ? project : project.getFolder( name );
	}

	public static void write( String content, IFile file ) throws CoreException
	{
		write( content.getBytes( StandardCharsets.UTF_8 ), file );
	}

	public static void write( byte[] content, IFile file ) throws CoreException
	{
		file.create( new ByteArrayInputStream( content ), false, null );
	}

	// Selections

	public static List<IProject> getSelectedProjects( ISelection selection, boolean hasNature, String nature ) throws CoreException
	{
		ArrayList<IProject> projects = new ArrayList<IProject>();
		if( selection instanceof IStructuredSelection )
		{
			for( Iterator<?> i = ( (IStructuredSelection) selection ).iterator(); i.hasNext(); )
			{
				Object object = i.next();
				if( object instanceof IAdaptable )
				{
					IProject project = (IProject) ( (IAdaptable) object ).getAdapter( IProject.class );
					if( ( project != null ) && ( ( nature == null ) || ( hasNature ? project.getNature( nature ) != null : project.getNature( nature ) == null ) ) )
						projects.add( project );
				}
			}
		}
		return projects;
	}

	// Natures

	public static void addNature( IProject project, String id ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();
		String[] natureIds = projectDescription.getNatureIds();
		String[] newNatureIds = new String[natureIds.length + 1];
		System.arraycopy( natureIds, 0, newNatureIds, 0, natureIds.length );
		newNatureIds[natureIds.length] = id;
		projectDescription.setNatureIds( newNatureIds );
		project.setDescription( projectDescription, null );
	}

	public static void removeNature( IProject project, String id ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();
		String[] natureIds = projectDescription.getNatureIds();
		for( int i = 0, length = natureIds.length; i < length; i++ )
		{
			if( natureIds[i].equals( id ) )
			{
				String[] newNatureIds = new String[length - 1];
				System.arraycopy( natureIds, 0, newNatureIds, 0, i );
				System.arraycopy( natureIds, i + 1, newNatureIds, i, length - i - 1 );
				projectDescription.setNatureIds( newNatureIds );
				project.setDescription( projectDescription, null );
				return;
			}
		}
	}

	// Builders

	public static ICommand getBuilder( IProject project, String id ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();

		ICommand[] commands = projectDescription.getBuildSpec();
		for( int i = 0, length = commands.length; i < length; i++ )
			if( commands[i].getBuilderName().equals( id ) )
				return commands[i];
		return null;
	}

	public static void addBuilder( IProject project, String id, Map<String, String> arguments ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();

		ICommand[] commands = projectDescription.getBuildSpec();
		for( ICommand command : commands )
		{
			if( command.getBuilderName().equals( id ) )
				return;
		}

		ICommand[] newCommands = new ICommand[commands.length + 1];
		System.arraycopy( commands, 0, newCommands, 1, commands.length );
		ICommand command = projectDescription.newCommand();
		command.setBuilderName( id );
		if( arguments != null )
			command.setArguments( arguments );
		newCommands[0] = command;
		projectDescription.setBuildSpec( newCommands );
		project.setDescription( projectDescription, null );
	}

	public static void removeBuilder( IProject project, String id ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();

		ICommand[] commands = projectDescription.getBuildSpec();
		for( int i = 0, length = commands.length; i < length; i++ )
		{
			if( commands[i].getBuilderName().equals( id ) )
			{
				ICommand[] newCommands = new ICommand[length - 1];
				System.arraycopy( commands, 0, newCommands, 0, i );
				System.arraycopy( commands, i + 1, newCommands, i, length - i - 1 );
				projectDescription.setBuildSpec( newCommands );
				project.setDescription( projectDescription, null );
				return;
			}
		}
	}

	// Classpath containers

	public static IClasspathContainer getClasspathContainer( IJavaProject project, IPath path ) throws JavaModelException
	{
		IClasspathEntry[] entries = project.getRawClasspath();
		for( IClasspathEntry entry : entries )
			if( ( entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) && ( entry.getPath().equals( path ) ) )
				return (IClasspathContainer) entry;
		return null;
	}

	public static void setClasspathContainer( IJavaProject project, IClasspathContainer container ) throws JavaModelException
	{
		IPath path = container.getPath();
		IClasspathEntry[] entries = project.getRawClasspath();

		int found = -1;
		for( int i = 0, length = entries.length; i < length; i++ )
		{
			IClasspathEntry entry = entries[i];
			if( ( entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) && ( entry.getPath().equals( path ) ) )
			{
				found = i;
				break;
			}
		}

		if( found == -1 )
		{
			IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
			System.arraycopy( entries, 0, newEntries, 0, entries.length );
			newEntries[entries.length] = JavaCore.newContainerEntry( container.getPath() );
			project.setRawClasspath( newEntries, null );
		}
		else
		{
			entries[found] = JavaCore.newContainerEntry( path );
			project.setRawClasspath( entries, null );
		}

		JavaCore.setClasspathContainer( Classpath.PATH, new IJavaProject[]
		{
			project
		}, new IClasspathContainer[]
		{
			container
		}, null );
	}

	public static void removeClasspathContainer( IJavaProject project, IPath path ) throws JavaModelException
	{
		IClasspathEntry[] entries = project.getRawClasspath();

		int found = -1;
		for( int i = 0, length = entries.length; i < length; i++ )
		{
			IClasspathEntry entry = entries[i];
			if( ( entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) && ( entry.getPath().equals( path ) ) )
			{
				found = i;
				break;
			}
		}

		if( found != -1 )
		{
			IClasspathEntry[] newEntries = new IClasspathEntry[entries.length - 1];
			System.arraycopy( entries, 0, newEntries, 0, found );
			if( found != entries.length )
				System.arraycopy( entries, found + 1, newEntries, found, entries.length - found - 1 );
			project.setRawClasspath( newEntries, null );
		}
	}

	// Consoles

	public static IOConsole getConsole( String name )
	{
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = manager.getConsoles();
		for( IConsole console : consoles )
			if( name.equals( console.getName() ) )
			{
				if( console instanceof IOConsole )
					return (IOConsole) console;
				else
					break;
			}
		IOConsole console = new IOConsole( name, null );
		manager.addConsoles( new IConsole[]
		{
			console
		} );
		return console;
	}

	// SWT

	public static Composite createComposite( Composite parent, int columns, int hspan, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace )
	{
		Composite composite = new Composite( parent, SWT.NONE );
		GridLayout layout = new GridLayout( columns, false );
		composite.setLayout( layout );
		composite.setFont( parent.getFont() );
		GridData data = new GridData( SWT.BEGINNING, SWT.BEGINNING, grabExcessHorizontalSpace, grabExcessVerticalSpace );
		data.horizontalSpan = hspan;
		composite.setLayoutData( data );
		return composite;
	}

	public static Composite createComposite( Composite parent )
	{
		return createComposite( parent, 1, 1, false, false );
	}

	public static Group createGroup( Composite parent, String text, int columns, int hspan, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace )
	{
		Group group = new Group( parent, SWT.NONE );
		GridLayout layout = new GridLayout( columns, false );
		group.setLayout( layout );
		group.setText( text );
		group.setFont( parent.getFont() );
		GridData data = new GridData( SWT.BEGINNING, SWT.BEGINNING, grabExcessHorizontalSpace, grabExcessVerticalSpace );
		data.horizontalSpan = hspan;
		group.setLayoutData( data );
		return group;
	}

	public static Combo createCombo( Composite parent, int style, int hspan, int fill, String[] items )
	{
		Combo combo = new Combo( parent, style );
		combo.setFont( parent.getFont() );
		GridData data = new GridData( fill );
		data.horizontalSpan = hspan;
		combo.setLayoutData( data );
		if( items != null )
			combo.setItems( items );
		combo.select( 0 );
		return combo;
	}

	public static void waitUntilDisposed( Shell shell )
	{
		while( !shell.isDisposed() )
			if( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private EclipseUtil()
	{
	}
}
