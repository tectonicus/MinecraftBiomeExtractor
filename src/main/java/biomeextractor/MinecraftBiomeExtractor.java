package biomeextractor;

import java.io.File;

public class MinecraftBiomeExtractor
{
	public static void main(String[] args) 
	{
		boolean noGui = false;
		boolean flush = false;
		boolean coordinatesOnly = false;
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
			else if (args[i].equalsIgnoreCase("-coordsOnly")) {
				coordinatesOnly = true;
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
		
		if (noGui && worldFolder == null)
		{
			System.out.print("You need to specify a world folder to process!");
			return;
		}
		
		if (outputDir == null)
		{
			outputDir = new File(worldFolder, "biomes");
		}
		
		if (showHelp)
		{
			System.out.println("Minecraft Biome Extractor command line usage:");
			System.out.println("\tjava -jar MinecraftBiomeExtractor.jar -nogui world_folder [-flush] [-quiet] [-coordsOnly] [-jar jarLocation]");
			System.out.println("\t-coordsOnly exports .biome coordinates without requiring or writing colormap PNGs.");
			System.out.println("\tYou can use the -jar option to launch the GUI with a manually secified JAR file.");
		}
		else if (!noGui)
		{
			if (manualJarLocation)
				MinecraftBiomeExtractorGUI.launchGUI(jarLocation);
			else
				MinecraftBiomeExtractorGUI.launchGUI(null);
		}
		else
		{
			// prevent OSX (and others) from opening a GUI
			System.setProperty("java.awt.headless", "true");
			
			if (worldFolder.isDirectory())
			{
				int exitCode = 0;
				// Create a world processor and bind to minecraft
				try (WorldProcessor worldProcessor = new WorldProcessor(null, errorsOnly, flush)) {
					if (manualJarLocation) {
						worldProcessor.setJarLocation(jarLocation);
					}
					if (!worldProcessor.bindToMinecraft()) {
						System.out.print("Failed to bind to Minecraft, cannot generate biomes.\n"
								+ "Review the above messages to see if there's anything you can do about it.\n"
								+ "If not, check online for a new version.");
						exitCode = 1;
					} else {
						worldProcessor.setWorldFolder(worldFolder);
						worldProcessor.setOutputDir(outputDir);
						worldProcessor.setCoordinatesOnly(coordinatesOnly);
						worldProcessor.run();
					}
				} catch (IllegalStateException e) {
					System.err.println("Extraction failed: " + e.getMessage());
					exitCode = 1;
				} catch (java.io.IOException e) {
					System.err.println("Could not release the Minecraft JAR: " + e.getMessage());
					exitCode = 1;
				}
				// Some historical client versions leave threads running after extraction.
				// Close the processor before exiting so its temporary JAR is removed.
				System.exit(exitCode);
			}
			else
			{
				System.out.println("Error: world folder not found.");
			}
		}
	}
}
