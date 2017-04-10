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

package com.threecrickets.creel;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Creel report.
 * 
 * @author Tal Liron
 */
public class Report
{
	//
	// Construction
	//

	public Report( Engine engine )
	{
		for( Module module : engine.getIdentifiedModules() )
			if( module.isExplicit() )
				modules.add( module );

		for( Artifact artifact : engine.getInstalledArtifacts() )
			artifacts.add( artifact );
	}

	public Report( State state )
	{
		for( Module module : state.getModules() )
			if( module.isExplicit() )
				modules.add( module );

		for( Artifact artifact : state.getArtifacts() )
			artifacts.add( artifact );
	}

	//
	// Attributes
	//

	public Iterable<Module> getModules()
	{
		return Collections.unmodifiableCollection( modules );
	}

	public Iterable<Artifact> getArtifacts()
	{
		return Collections.unmodifiableCollection( artifacts );
	}

	public Iterable<Artifact> getArtifacts( Module module )
	{
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		String moduleIdentifier = module.getIdentifier().toString();
		for( Artifact artifact : getArtifacts() )
			if( moduleIdentifier.equals( artifact.getModuleIdentifier() ) )
				artifacts.add( artifact );
		return Collections.unmodifiableCollection( artifacts );
	}

	//
	// Operations
	//

	public void print( PrintWriter writer )
	{
		for( Module module : getModules() )
			printModule( writer, module, 0 );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void printModule( PrintWriter writer, Module module, int depth )
	{
		for( int i = 0; i < depth; i++ )
			writer.print( ' ' );
		writer.println( module.getIdentifier() );
		for( Artifact artifact : getArtifacts( module ) )
		{
			for( int i = 0; i < depth + 1; i++ )
				writer.print( ' ' );
			writer.println( artifact.getFile() );
		}
		for( Module dependency : module.getDependencies() )
			printModule( writer, dependency, depth + 1 );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ArrayList<Module> modules = new ArrayList<Module>();

	private final ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
}
