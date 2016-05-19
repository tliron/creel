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

package com.threecrickets.creel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.threecrickets.creel.exception.UnsupportedPlatformException;
import com.threecrickets.creel.util.ClassUtil;

/**
 * The Creel factory is used to dynamically instantiate the base classes
 * according to supported platforms.
 * <p>
 * By default, the Maven platform is supported as "mvn". It is also the default
 * platform used when none is specified.
 * <p>
 * To install your own platform, use {@link #setPlatform(String, String)}. The
 * second argument is a prefix for all class name. For example, if you want to
 * create an "ivy" platform, your classes might be named:
 * <ul>
 * <li>org.myorg.creel.IvyRepository</li>
 * <li>org.myorg.creel.IvyModuleSpecification</li>
 * <li>org.myorg.creel.IvyModuleIdentifier</li>
 * </ul>
 * In this case, the prefix should be "org.myorg.creel.Ivy".
 * 
 * @author Tal Liron
 */
public class Factory
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * <p>
	 * By default supports the Maven platform as "mvn".
	 */
	public Factory()
	{
		setPlatform( "mvn", "com.threecrickets.creel.maven.Maven" );
	}

	//
	// Attributes
	//

	/**
	 * The supported platforms.
	 * 
	 * @return A map of platform names to class prefixes
	 */
	public Map<String, String> getPlatforms()
	{
		return Collections.unmodifiableMap( platforms );
	}

	/**
	 * Adds support for a platform.
	 * 
	 * @param name
	 *        The platform name
	 * @param prefix
	 *        The class prefix
	 */
	public void setPlatform( String name, String prefix )
	{
		platforms.put( name, prefix );
	}

	/**
	 * The default platform to use if none is specified. Defaults to "mvn".
	 * 
	 * @return The default platform name
	 */
	public String getDefaultPlatform()
	{
		return defaultPlatform;
	}

	/**
	 * The default platform to use if none is specified. Defaults to "mvn".
	 * 
	 * @param defaultPlatform
	 *        The default platform name
	 */
	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	//
	// Operations
	//

	/**
	 * Creates a repository instance for a platform.
	 * 
	 * @param platform
	 *        The platform or null to use default platform
	 * @param config
	 *        The config
	 * @return The repository instance
	 */
	public Repository newRepository( String platform, Map<String, ?> config )
	{
		return newInstance( platform, Repository.class.getSimpleName(), config );
	}

	/**
	 * Creates a module specification instance for a platform.
	 * 
	 * @param platform
	 *        The platform or null to use default platform
	 * @param config
	 *        The config
	 * @return The module specification instance
	 */
	public ModuleSpecification newModuleSpecification( String platform, Map<String, ?> config )
	{
		return newInstance( platform, ModuleSpecification.class.getSimpleName(), config );
	}

	/**
	 * Creates a module identifier instance for a platform.
	 * 
	 * @param platform
	 *        The platform or null to use default platform
	 * @param config
	 *        The config
	 * @return The module identifier instance
	 */
	public ModuleIdentifier newModuleIdentifier( String platform, Map<String, ?> config )
	{
		return newInstance( platform, ModuleIdentifier.class.getSimpleName(), config );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, String> platforms = new HashMap<String, String>();

	private String defaultPlatform = "mvn";

	private <T> T newInstance( String platform, String baseClassName, Map<String, ?> config )
	{
		if( platform == null )
			platform = getDefaultPlatform();
		String prefix = getPlatforms().get( platform );
		if( prefix == null )
			throw new UnsupportedPlatformException( platform );
		String className = prefix + baseClassName;
		return ClassUtil.newInstance( className, config );
	}
}
