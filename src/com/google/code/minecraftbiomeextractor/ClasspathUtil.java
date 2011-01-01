package com.google.code.minecraftbiomeextractor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClasspathUtil
{

	private static final Class<?>[] parameters = new Class[] { URL.class };
	
	/**
	 * Adds the content pointed by the URL to the classpath.
	 * 
	 * @param u
	 *            the URL pointing to the content to be added
	 * @throws IOException
	 */
	public static void addJarToClasspath(File jarFile) throws IOException
	{
		URL u = jarFile.toURI().toURL();
		URLClassLoader sysLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class<?> sysClass = URLClassLoader.class;
		try
		{
			Method method = sysClass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysLoader, new Object[] { u });
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}
	}

}
