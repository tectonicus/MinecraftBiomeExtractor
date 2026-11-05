package com.google.code.minecraftbiomeextractor;

public class Os
{
	public static boolean isWindows()
	{
		final String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("win") >= 0;
	}
	
	public static boolean isMac()
	{
		final String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("mac") >= 0;
	}
	
	public static boolean isNix()
	{
		final String osName = System.getProperty("os.name").toLowerCase();
		return osName.indexOf("nix") >=0 || osName.indexOf("nux") >=0;
	}
}
