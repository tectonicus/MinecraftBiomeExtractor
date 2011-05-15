package com.google.code.minecraftbiomeextractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	
	public static List<File> getWorldDirs()
	{
		List<File> worldDirs = new ArrayList<File>();
		File savedir = new File(findMinecraftDir(),"saves");
		
		if (savedir.exists() && savedir.isDirectory())
		{
			File[] potentialWorlds = savedir.listFiles();
			for(int i = 0; i< potentialWorlds.length; i++)
			{
				if (potentialWorlds[i].isDirectory() && (new File(potentialWorlds[i],"level.dat")).exists() && (new File(potentialWorlds[i],"region")).isDirectory())
				{
					// If the potential world is a directory, it contains a level.dat and a region folder
					worldDirs.add(potentialWorlds[i]);
				}
			}
		}
		return worldDirs;
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
