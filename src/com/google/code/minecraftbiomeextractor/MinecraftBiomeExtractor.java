package com.google.code.minecraftbiomeextractor;

import java.io.File;

public class MinecraftBiomeExtractor
{
	public static void main(String[] args) 
	{
		boolean noGui = false;
		boolean flush = false;
		boolean errorsOnly = false;
		File jarLocation = null;
		File outputDir = null;
		boolean manualJarLocation = false;
		boolean showHelp = false;
		File worldFolder = null;
		
		int i = 0;
		while (i < args.length)
		{
			int remaining = args.length - i - 1;
			
			if (args[i].equalsIgnoreCase("-nogui"))
			{
				noGui = true;
			}
			else if (args[i].equalsIgnoreCase("-flush"))
			{
				flush = true;
			}
			else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("?"))
			{
				showHelp = true;
			}
			else if (args[i].equalsIgnoreCase("-quiet"))
			{
				errorsOnly = true;
			}
			else if (args[i].equalsIgnoreCase("-jar") && remaining > 0)
			{
				i++;
				jarLocation = new File(args[i]);
				if (jarLocation.exists())
					manualJarLocation = true;
				else
					System.out.print("Minecraft jar location was invalid.");
			}
			else if (args[i].equalsIgnoreCase("-outputDir") && remaining > 0)
			{
				i++;
				outputDir = new File(args[i]);
			}
			else
			{
				worldFolder = new File(args[i]);
			}
			
			i++;
		}
		
		if (outputDir == null)
		{
			outputDir = new File(worldFolder, "EXTRACTEDBIOMES");
		}
		
		if (showHelp)
		{
			System.out.println("Minecraft Biome Extractor command line usage:");
			System.out.println("\tjava -jar MinecraftBiomeExtractor.jar -nogui world_folder [-flush] [-quiet] [-jar jarloaction]");
		}
		else if (!noGui)
		{
			MinecraftBiomeExtractorGUI.launchGUI();
		}
		else
		{
			if (worldFolder.isDirectory())
			{
				// Create a world processor and bind to minecraft
				WorldProcessor worldProcessor = new WorldProcessor(null, errorsOnly, flush);
				
				if (manualJarLocation)
					worldProcessor.setJarLocation(jarLocation);
				
				final boolean bound = worldProcessor.bindToMinecraft();
				if (!bound)
				{
					System.out.print("Failed to bind to Minecraft, cannot generate biomes.\n" 
							 + "Review the above messages to see if there's anything you can do about it.\n"
							 + "If not, check online for a new version.");
				}
				else
				{
					worldProcessor.setWorldFolder(worldFolder);
					worldProcessor.setOutputDir(outputDir);
					worldProcessor.run();
				}
			}				
			else
			{
				System.out.println("Error: world folder not found.");
			}
		}
	}

}
