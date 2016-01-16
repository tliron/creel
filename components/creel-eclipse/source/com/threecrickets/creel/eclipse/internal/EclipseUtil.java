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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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

	/**
	 * Interpolates and gets a file relative to the project root.
	 * 
	 * @param project
	 *        The project
	 * @param name
	 *        The file name relative to the project root
	 * @return The file or null
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static IFile getInterpolatedFile( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		return !name.isEmpty() ? project.getFile( name ) : null;
	}

	/**
	 * Interpolates and gets a folder relative to the project root. Note that
	 * "." will mean the project root, but an empty string will mean <i>no
	 * <i> folder and will return null.
	 * 
	 * @param project
	 *        The project
	 * @param name
	 *        The folder name relative to the project root
	 * @return The folder or null
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static IContainer getInterpolatedFolder( IProject project, String name ) throws CoreException
	{
		IStringVariableManager manager = VariablesPlugin.getDefault().getStringVariableManager();
		name = manager.performStringSubstitution( name );
		if( name.isEmpty() )
			return null;
		// Note: project.getFolder cannot get the root! (?)
		return ".".equals( name ) ? project : project.getFolder( name );
	}

	/**
	 * Writes to an Eclipse file. (Eclipse will automatically refresh all views,
	 * run builders, etc.)
	 * 
	 * @param content
	 *        The content
	 * @param file
	 *        The file
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static void write( String content, IFile file ) throws CoreException
	{
		write( content.getBytes( StandardCharsets.UTF_8 ), file );
	}

	/**
	 * Writes to an Eclipse file. (Eclipse will automatically refresh all views,
	 * run builders, etc.)
	 * 
	 * @param content
	 *        The content
	 * @param file
	 *        The file
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static void write( byte[] content, IFile file ) throws CoreException
	{
		file.create( new ByteArrayInputStream( content ), false, null );
	}

	// Selections

	/**
	 * Finds all projects currently selected by the user. Can optionally filter
	 * by nature.
	 * 
	 * @param selection
	 *        The selection
	 * @param hasNature
	 *        Whether to also check for nature
	 * @param nature
	 *        The nature ID (used when hasNature is true)
	 * @return The selected projects
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static Iterable<IProject> getSelectedProjects( ISelection selection, boolean hasNature, String nature ) throws CoreException
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

	/**
	 * Adds a nature to a project.
	 * 
	 * @param project
	 *        The project
	 * @param id
	 *        The nature ID
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
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

	/**
	 * Removes a nature from a project if it is there.
	 * 
	 * @param project
	 *        The project
	 * @param id
	 *        The nature ID
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
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

	/**
	 * Gets a builder from a project. (Assumes that the builder ID is only used
	 * once.)
	 * 
	 * @param project
	 *        The project
	 * @param id
	 *        The builder ID
	 * @return The builder or null
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static ICommand getBuilder( IProject project, String id ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();

		ICommand[] commands = projectDescription.getBuildSpec();
		for( int i = 0, length = commands.length; i < length; i++ )
			if( commands[i].getBuilderName().equals( id ) )
				return commands[i];
		return null;
	}

	/**
	 * Sets a builder in a project. If the builder is already there, will update
	 * its arguments. (Assumes that the builder ID is only used once.)
	 * 
	 * @param project
	 *        The project
	 * @param id
	 *        The builder ID
	 * @param arguments
	 *        The builder arguments or null
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
	public static void setBuilder( IProject project, String id, Map<String, String> arguments ) throws CoreException
	{
		IProjectDescription projectDescription = project.getDescription();

		int found = -1;
		ICommand[] commands = projectDescription.getBuildSpec();
		for( int i = 0, length = commands.length; i < length; i++ )
		{
			ICommand command = commands[i];
			if( command.getBuilderName().equals( id ) )
			{
				found = i;
				break;
			}
		}

		ICommand command = projectDescription.newCommand();
		command.setBuilderName( id );
		if( arguments != null )
			command.setArguments( arguments );

		if( found == -1 )
		{
			ICommand[] newCommands = new ICommand[commands.length + 1];
			System.arraycopy( commands, 0, newCommands, 1, commands.length );
			newCommands[0] = command;
			projectDescription.setBuildSpec( newCommands );
		}
		else
		{
			commands[found] = command;
			projectDescription.setBuildSpec( commands );
		}

		project.setDescription( projectDescription, null );
	}

	/**
	 * Removes a builder from project if it is there. (Assumes that the builder
	 * ID is only used once.)
	 * 
	 * @param project
	 *        The project
	 * @param id
	 *        The builder ID
	 * @throws CoreException
	 *         In case of an Eclipse error
	 */
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

	/**
	 * Gets a classpath container from a project. (Assumes that the path is only
	 * used once.)
	 * 
	 * @param project
	 *        The project
	 * @param path
	 *        The path
	 * @return The classpath container or null
	 * @throws JavaModelException
	 *         In case of an Eclipse JDT error
	 */
	public static IClasspathContainer getClasspathContainer( IJavaProject project, IPath path ) throws JavaModelException
	{
		IClasspathEntry[] entries = project.getRawClasspath();
		for( IClasspathEntry entry : entries )
			if( ( entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) && ( entry.getPath().equals( path ) ) )
				return (IClasspathContainer) entry;
		return null;
	}

	/**
	 * Sets a classpath container in a project. If the classpath container is
	 * already there, will recreate it. (Assumes that the path is only used
	 * once.)
	 * 
	 * @param project
	 *        The project
	 * @param container
	 *        The classpath container
	 * @throws JavaModelException
	 *         In case of an Eclipse JDT error
	 */
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

	/**
	 * Removes a classpath container from project if it is there. (Assumes that
	 * the path is only used once.)
	 * 
	 * @param project
	 *        The project
	 * @param path
	 *        The path
	 * @throws JavaModelException
	 *         In case of an Eclipse JDT error
	 */
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

	/**
	 * Gets a console by name, creating it if it's not already there.
	 * 
	 * @param name
	 *        The console name
	 * @return The console
	 */
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

	/**
	 * Creates a composite with a grid layout, which can be nested in a parent
	 * with a grid layout.
	 * 
	 * @param parent
	 *        The parent
	 * @param columns
	 *        The number of columns
	 * @param hspan
	 *        The horizontal span in the parent
	 * @param grabExcessHorizontalSpace
	 *        True to grab excess horizontal space in the parent
	 * @param grabExcessVerticalSpace
	 *        True to grab excess vertical space in the parent
	 * @return The composite
	 */
	public static Composite createGrid( Composite parent, int columns, int hspan, boolean grabExcessHorizontalSpace, boolean grabExcessVerticalSpace )
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

	/**
	 * Creates a composite with a grid layout, which can be nested in a parent
	 * with a grid layout.
	 * 
	 * @param parent
	 *        The parent
	 * @return The composite
	 */
	public static Composite createGrid( Composite parent )
	{
		return createGrid( parent, 1, 1, false, false );
	}

	/**
	 * Creates a group with a grid layout, which can be nested in a parent with
	 * a grid layout.
	 * 
	 * @param parent
	 *        The parent
	 * @param text
	 *        The group text
	 * @param columns
	 *        The number of columns
	 * @param hspan
	 *        The horizontal span in the parent
	 * @param grabExcessHorizontalSpace
	 *        True to grab excess horizontal space in the parent
	 * @param grabExcessVerticalSpace
	 *        True to grab excess vertical space in the parent
	 * @return
	 */
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

	/**
	 * Creates a text widget with an optional label and default value, which can
	 * be nested in a parent with a grid layout, in which case it will the
	 * column.
	 * 
	 * @param parent
	 *        The parent
	 * @param label
	 *        The label or null
	 * @param argument
	 *        The argument name or null
	 * @param arguments
	 *        The arguments or null
	 * @return
	 */
	public static Text createText( Composite parent, String label, String argument, Map<String, String> arguments )
	{
		if( label != null )
			new Label( parent, 0 ).setText( label );
		Text text = new Text( parent, SWT.SINGLE | SWT.BORDER );
		text.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
		if( ( arguments != null ) && ( argument != null ) )
		{
			String value = arguments.get( argument );
			if( value != null )
				text.setText( value );
		}
		return text;
	}

	/**
	 * Reads-and-dispatches the shell loop until it is disposed.
	 * 
	 * @param shell
	 *        The shell
	 */
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
