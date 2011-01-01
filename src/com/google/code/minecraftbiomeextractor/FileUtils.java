package com.google.code.minecraftbiomeextractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class FileUtils
{
	public static boolean deleteDirectory(File path)
	{
		if (!path.exists())
			return true;
		
		File[] files = path.listFiles();
		for (int i=0; i<files.length; i++)
		{
			if (files[i].isDirectory())
			{
				deleteDirectory(files[i]);
			}
			else
			{
				files[i].delete();
			}
		}
		
		return path.delete();
	}
	
	public static boolean areIdentical(File lhs, File rhs)
	{
		try
		{
			return areDirectoriesIdentical(lhs, rhs);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	private static boolean areDirectoriesIdentical(File lhsDir, File rhsDir) throws Exception
	{
		if (lhsDir == null && rhsDir == null)
			return true;
		
		if (lhsDir.isFile() && rhsDir.isFile())
		{
			// Check as files
			return areFilesIdentical(lhsDir, rhsDir);
		}
		else if (lhsDir.isDirectory() && rhsDir.isDirectory())
		{
			File[] lhsEntries = lhsDir.listFiles();
			File[] rhsEntries = rhsDir.listFiles();
			
			Arrays.sort(lhsEntries);
			Arrays.sort(rhsEntries);
			
		//	if (lhsEntries.length != rhsEntries.length)
		//		return false;
			
			for (File lhs : lhsEntries)
			{
				boolean found = false;
				
				if (lhs.getName().equals(".svn"))
					found = true;
				
				for (File rhs : rhsEntries)
				{
					if (lhs.getName().equals(rhs.getName()))
					{
						final boolean identical = areDirectoriesIdentical(lhs, rhs);
						if (!identical)
							return false;
						
						found = true;
						break;
					}
				}
				
				if (!found)
					return false;
			}
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private static boolean areFilesIdentical(File lhs, File rhs) throws Exception
	{
		if (lhs == null && rhs == null)
			return true;
		
		if (lhs.length() != rhs.length())
			return false;
		
		FileInputStream lhsIn = new FileInputStream(lhs);
		FileInputStream rhsIn = new FileInputStream(rhs);
		
		BufferedInputStream lhsBuf = new BufferedInputStream(lhsIn);
		BufferedInputStream rhsBuf = new BufferedInputStream(rhsIn);
		
		while (true)
		{
			final int lhsVal = lhsBuf.read();
			final int rhsVal = rhsBuf.read();
			
			if (lhsVal != rhsVal)
				return false;
			
			if (lhsVal == -1 || rhsVal == -1)
				break;
		}
		return true;
	}
}
