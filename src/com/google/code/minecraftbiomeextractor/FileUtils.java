package com.google.code.minecraftbiomeextractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

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
	
	public static void copyFiles(File src, File dest, Set<String> excludeExtensions) throws IOException
	{
		// Check to ensure that the source is valid...
		if (!src.exists())
		{
			throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
		}
		else if (!src.canRead())
		{
			// check to ensure we have rights to the source...
			throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
		}
		
		// is this a directory copy?
		if (src.isDirectory())
		{
			if (!dest.exists())
			{ // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)
				if (!dest.mkdirs())
				{
					throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
				}
			}
			// get a listing of files...
			String list[] = src.list();
			// copy all the files in the list.
			for (int i = 0; i < list.length; i++)
			{
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				copyFiles(src1, dest1, excludeExtensions);
			}
		}
		else
		{
			String extension = getExtension(src.getName());
			if (!excludeExtensions.contains(extension))
			{
				// This was not a directory, so lets just copy the file
				FileInputStream fin = null;
				FileOutputStream fout = null;
				byte[] buffer = new byte[4096]; // Buffer 4K at a time
				
				int bytesRead;
				try
				{
					// open the files for input and output
					fin = new FileInputStream(src);
					fout = new FileOutputStream(dest);
					// while bytesRead indicates a successful read, lets write...
					while ((bytesRead = fin.read(buffer)) >= 0)
					{
						fout.write(buffer, 0, bytesRead);
					}
				}
				catch (IOException e)
				{
					// Error copying file...
					IOException wrapper = new IOException("copyFiles: Unable to copy file: "
															+ src.getAbsolutePath() + " to "
															+ dest.getAbsolutePath() + ".");
					wrapper.initCause(e);
					wrapper.setStackTrace(e.getStackTrace());
					throw wrapper;
				}
				finally
				{
					// Ensure that the files are closed (if they were open).
					if (fin != null)
					{
						fin.close();
					}
					if (fout != null)
					{
						fout.close();
					}
				}
			}
			else
			{
				System.out.println("Skipping "+src.getAbsolutePath());
			}
		}
	}
	
	public static String getExtension(String file)
	{
		final int dotPos = file.lastIndexOf('.');
		String ext = file.substring(dotPos+1, file.length());
		return ext;
	}
}
