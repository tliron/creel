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

package com.threecrickets.creel.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Class and instantiation utilities.
 * 
 * @author Tal Liron
 */
public abstract class ClassUtil
{
	//
	// Static operations
	//

	@SuppressWarnings("unchecked")
	public static <T> T newInstance( String className, Map<String, ?> config )
	{
		try
		{
			Class<T> theClass = (Class<T>) Class.forName( className );
			Constructor<T> constructor = theClass.getConstructor( Map.class );
			return constructor.newInstance( config );
		}
		catch( ClassNotFoundException x )
		{
			throw new RuntimeException( "Could not find class: " + className, x );
		}
		catch( NoSuchMethodException x )
		{
			throw new RuntimeException( "Class does not have a config constructor: " + className, x );
		}
		catch( SecurityException x )
		{
			throw new RuntimeException( "Could not access class: " + className, x );
		}
		catch( InstantiationException x )
		{
			throw new RuntimeException( "Class error: " + className, x );
		}
		catch( IllegalAccessException x )
		{
			throw new RuntimeException( "Could not access class: " + className, x );
		}
		catch( IllegalArgumentException x )
		{
			throw new RuntimeException( "Class error: " + className, x );
		}
		catch( InvocationTargetException x )
		{
			throw new RuntimeException( x.getCause().getMessage(), x.getCause() );
		}
	}

	/**
	 * Executes the main() method of the class named by the first argument.
	 * 
	 * @param classLoader
	 *        The class loader
	 * @param arguments
	 *        The class name followed by the arguments for main()
	 */
	public static void main( ClassLoader classLoader, String[] arguments )
	{
		String className = arguments[0];
		String[] mainArguments = new String[arguments.length - 1];
		System.arraycopy( arguments, 1, mainArguments, 0, mainArguments.length );
		main( classLoader, className, mainArguments );
	}

	/**
	 * Executes the main() method of a class.
	 * 
	 * @param classLoader
	 *        The class loader
	 * @param className
	 *        The class name
	 * @param arguments
	 *        The arguments for main()
	 */
	public static void main( ClassLoader classLoader, String className, String[] arguments )
	{
		try
		{
			Class<?> theClass = Class.forName( className, true, classLoader );
			Method mainMethod = theClass.getMethod( "main", String[].class );
			mainMethod.invoke( null, (Object) arguments );
		}
		catch( ClassNotFoundException x )
		{
			throw new RuntimeException( "Could not find class: " + className, x );
		}
		catch( SecurityException x )
		{
			throw new RuntimeException( "Could not access class: " + className, x );
		}
		catch( NoSuchMethodException x )
		{
			throw new RuntimeException( "Class does not have a main method: " + className, x );
		}
		catch( IllegalArgumentException x )
		{
			throw new RuntimeException( "Class error: " + className, x );
		}
		catch( IllegalAccessException x )
		{
			throw new RuntimeException( "Could not access class: " + className, x );
		}
		catch( InvocationTargetException x )
		{
			throw new RuntimeException( x.getCause().getMessage(), x.getCause() );
		}
	}

	public static <T extends Enum<?>> T valueOfNonStrict( Class<T> enumeration, String value )
	{
		for( T e : enumeration.getEnumConstants() )
		{
			String name = e.name().replaceAll( "_", "" );
			if( name.equalsIgnoreCase( value ) )
				return e;
		}
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ClassUtil()
	{
	}
}
