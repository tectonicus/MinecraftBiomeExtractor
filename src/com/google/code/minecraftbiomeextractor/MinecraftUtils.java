package com.google.code.minecraftbiomeextractor;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class MinecraftUtils
{
	public static File findMinecraftDir()
	{
		File minecraftFolderPath;
		
		if (Os.isMac())
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),"Library");
			minecraftFolderPath = new File(minecraftFolderPath,"Application Support");
			minecraftFolderPath = new File(minecraftFolderPath,"minecraft");
		}
		else if (Os.isWindows())
		{
			minecraftFolderPath = new File(System.getenv("APPDATA"),".minecraft");
		}
		else if (Os.isNix())
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
		}
	    else
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
		}
		
		return minecraftFolderPath;
	}
	
	public static File findBinaryDir(File minecraftDir)
	{
		if (minecraftDir == null)
			return null;
		
		return new File(minecraftDir, "bin");
	}
	
	public static File findMinecraftJar(File minecraftDir)
	{
		if (minecraftDir == null)
			return null;
		
		return new File(findBinaryDir(minecraftDir), "minecraft.jar");
	}
}
