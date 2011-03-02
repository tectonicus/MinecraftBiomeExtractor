package com.google.code.minecraftbiomeextractor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class WorldProcessor implements Runnable
{
	public enum ColourType
	{
		GrassColour,
		FoliageColour
	}
	
	private static final String NEW_LINE = "\n";
	
	private static final boolean printClassRefStrings = false;
	
	// Client and Common Signatures
	// Class names as of 1.3_01
	/*ec*/private static final String SAVE_HANDLER_CLASS_REF = "carg:class carg:class carg:boolean +ret:class+ret:void+arg:class ret:interface+ret:class+arg:class arg:interface ret:void+arg:class ret:void+ret:void+fld:class+fld:class+fld:class+fld:long+";
	/*rw*/private static final String ABSTRACT_SAVE_HANDLER_REF = "ret:class+ret:void+arg:class ret:interface+arg:class arg:interface ret:void+arg:class ret:void+";
	/*dt*/private static final String SAVE_CLASS_REF = "carg:class carg:class +carg:interface carg:class carg:long +carg:interface carg:class carg:long carg:class +carg:interface carg:class carg:class carg:long +arg:int arg:int arg:int ret:boolean+ret:void+ret:long+arg:int arg:int arg:int ret:boolean+ret:interface+arg:int arg:int ret:class+arg:float ret:class+arg:int arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int arg:int ret:void+ret:void+arg:int arg:int arg:int ret:float+arg:class ret:void+arg:class ret:boolean+arg:int arg:int arg:int arg:class ret:void+arg:class arg:int arg:int arg:int arg:int ret:void+ret:interface+arg:class ret:void+arg:class ret:boolean+arg:int arg:int ret:class+arg:interface ret:void+arg:int arg:int arg:int arg:int ret:boolean+arg:float ret:float+arg:interface ret:void+arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:class ret:int+arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:class ret:interface+arg:int arg:int arg:int ret:class+arg:class arg:class ret:boolean+arg:int arg:int arg:int ret:boolean+ret:long+ret:class+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int ret:boolean+arg:int arg:int arg:int ret:boolean+ret:boolean+arg:class arg:double arg:double arg:double arg:double arg:double arg:double ret:void+arg:int arg:int ret:int+arg:class ret:void+arg:boolean arg:interface ret:void+arg:int ret:boolean+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:boolean ret:int+arg:class arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int ret:int+arg:class arg:class ret:class+arg:class arg:class arg:boolean ret:class+arg:class arg:class arg:float arg:float ret:void+arg:double arg:double arg:double arg:class arg:float arg:float ret:void+arg:class arg:int arg:int arg:int ret:void+ret:class+arg:class ret:boolean+arg:interface ret:void+arg:class arg:class ret:interface+arg:float ret:int+arg:class arg:float ret:class+arg:class arg:boolean ret:void+arg:class ret:boolean+arg:class arg:class arg:class ret:boolean+arg:class arg:class ret:boolean+arg:class arg:double arg:double arg:double arg:float ret:class+arg:class arg:double arg:double arg:double arg:float arg:boolean ret:class+arg:class arg:class ret:float+arg:class ret:class+arg:int arg:int arg:int arg:class ret:void+arg:interface ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int arg:boolean ret:void+arg:boolean arg:boolean ret:void+arg:boolean ret:boolean+arg:class arg:class ret:interface+arg:interface ret:void+arg:int arg:int arg:int arg:int arg:boolean ret:boolean+arg:class arg:class arg:float ret:class+arg:class arg:int arg:int arg:int arg:float ret:class+arg:class arg:double ret:class+arg:double arg:double arg:double arg:double ret:class+arg:int arg:int arg:int arg:int arg:int arg:int arg:class ret:void+arg:long ret:void+arg:class ret:void+arg:class arg:int arg:int arg:int ret:boolean+arg:class arg:byte ret:void+arg:int arg:int arg:int ret:void+ret:void+ret:void+ret:interface+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:void+ret:void+arg:int arg:int arg:int ret:void+ret:boolean+arg:int arg:int arg:int arg:int ret:void+ret:class+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:boolean+ret:void+ret:class+arg:class ret:void+arg:int arg:int arg:int arg:int ret:boolean+arg:float ret:float+arg:int arg:int arg:int ret:int+ret:boolean+arg:int arg:int ret:int+arg:int arg:int arg:int ret:void+arg:int arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int arg:int ret:boolean+ret:void+ret:class+arg:int arg:int arg:int ret:void+ret:void+arg:float ret:class+arg:int arg:int arg:int arg:int arg:int ret:void+ret:void+arg:class ret:void+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int ret:boolean+arg:int arg:int ret:int+ret:void+arg:class ret:void+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int ret:int+ret:void+arg:int arg:int arg:int ret:class+fld:boolean+fld:interface+fld:interface+fld:interface+fld:class+fld:interface+fld:interface+fld:interface+fld:long+fld:int+fld:int+fld:int+fld:boolean+fld:long+fld:int+fld:int+fld:class+fld:boolean+fld:class+fld:interface+fld:interface+fld:interface+fld:class+fld:boolean+fld:boolean+fld:class+fld:int+fld:boolean+fld:boolean+fld:int+fld:interface+fld:int+fld:interface+fld:boolean+";
	/*su*/private static final String BIOME_GEN_CLASS_REF = "+carg:class +arg:int arg:int ret:double+arg:int arg:int ret:class+arg:class ret:class+arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+";
	
	// Server-Specific Signatures
	/*cw*/ // Server save handler - client works fine
	/*mi*/ // Server abstract save handler - client works fine
	/*nc*/ // Biome Gen - client works fine
	/*cp*/ // Save Class - client works fine
	/*mn*/private static final String SERVER_RAND_CLASS_REF = "+ret:interface+ret:void+ret:void+arg:class ret:void+arg:int arg:int ret:boolean+arg:long arg:float ret:float+arg:int ret:class+fld:class+fld:class+fld:boolean+fld:boolean+fld:boolean+fld:class+fld:int+fld:class+";
	
	private MinecraftBiomeExtractorGUI gui = null;
	
	// Minecraft Class Bindings
	private File minecraftJar = null;
	private ArrayList<String> classListing;
	private ArrayList<String> class_signatures;
	private boolean useGUI = true;
	private Class<?> saveHandlerClass;
	private Class<?> minecraftSaveClass;
	private Class<?> biomeGeneratorClass;
	private Class<?> abstractSaveHandlerClass;
	private Class<?> serverRandomClass;
	private Constructor<?> handleMinecraftSave;
	private Constructor<?> createMinecraftSave;
	private Constructor<?> createBiomeGenerator;
	private Method generateForLoaction;
    private Field loadedField = null;
    private Field genTemp;
    private Field genMoist;
    private Object saveHandler;
    private Object minecraftSave;
    private Object biomeGenerator;
	private Object argList[]; // Used to call the biome generator.
    
    // Storage for the grasscolor and foliagecolor pngs
	private BufferedImage grassColourImage = null;
	private BufferedImage foliageColourImage = null;
	private boolean useDefaultBiomeImages = true;
	
	// World Variables
	private File worldFolder = null;
	private ArrayList<int[]> chunks;
	
	// Output
	private File outputDir = null;
	
	// Internal class state variables
	private boolean flush = false;
    boolean isServerJar = false;
	private boolean errorsOnly = false;
	private int lastPercent = 0;
	private int countPercents = 0;
	
	private boolean bound = false;	// Is set to true if the extractor bound to Minecraft properly
	private boolean die = false;  	// Set to true to abort the process if run in a separate thread.
	
	public WorldProcessor()
	{
		this.useGUI = false;
		this.errorsOnly = true;
		
        // Setup a parameter array we'll use to invoke minecraft's biome generator
		setupBiomeGenArgs();
		setupDefaultBiomeImages();
	}
	
	public WorldProcessor(MinecraftBiomeExtractorGUI gui, final boolean errorsOnly, final boolean flush)
	{
		if (gui == null)
			useGUI = false;
		else
			this.gui = gui;
		
		this.flush = flush;
		this.errorsOnly = errorsOnly;
		
        // Setup a parameter array we'll use to invoke minecraft's biome generator
		setupBiomeGenArgs();
		setupDefaultBiomeImages();
		
	}
	
	private void setupDefaultBiomeImages()
	{
		try
		{
			grassColourImage = ImageIO.read(WorldProcessor.class.getResource("/grasscolor_mbe_fallback.png"));
			foliageColourImage = ImageIO.read(WorldProcessor.class.getResource("/foliagecolor_mbe_fallback.png"));
		}
		catch (IOException e)
		{
			printm("Warning, could not load default biome color images." + NEW_LINE);
		}
	}
	
	private void setupBiomeGenArgs()
	{
        // Setup a parameter array we'll use later when invoking it
        argList = new Object[4];
        argList[0] = Integer.valueOf(0);
        argList[1] = Integer.valueOf(0);
        argList[2] = Integer.valueOf(1);
        argList[3] = Integer.valueOf(1);
	}
	
	public boolean isBound()
	{
		return bound;
	}
	
	public void stopRunning()
	{
		die = true;
		return;
	}
	
	public boolean setWorldFolder(File world_folder)
	{
		if (world_folder.exists() && world_folder.isDirectory())
		{
			this.worldFolder = world_folder;
			return true;
		}
		else
		{
			printe("World folder does not exist." + NEW_LINE);
			this.worldFolder = null;
			return false;
		}
	}
	
	public void setOutputDir(File dir)
	{
		this.outputDir = dir;
	}
	
	public void setFlush(boolean flush)
	{
		this.flush = flush;
	}
	
	public void run()
	{
		// Reset some key variables
		die = false;
		lastPercent = 0;
		countPercents = 0;
		
		// Make sure the world folder has been specified.
		if (worldFolder == null)
		{
			printe("No world folder specified."+ NEW_LINE);
			return;
		}
		
		if (outputDir == null)
		{
			outputDir = new File(worldFolder,"biomes");
			printm("No output dir specified." + NEW_LINE + "Placing output in "+ outputDir.getAbsolutePath() + NEW_LINE);
		}
		
		if (outputDir.exists() && flush)
		{
			printm("Deleting existing biome data in " + outputDir.getAbsolutePath() + NEW_LINE);
			FileUtils.deleteDirectory(outputDir);
		}
	
		printm("Opening " + worldFolder.getName() + "..." + NEW_LINE);
		
		this.setupDataFolder(outputDir);

		printm("Locating Minecraft save..."+NEW_LINE);
		if (!this.loadWorld())
		{
			printe("Minecraft classes failed to load the world."+ NEW_LINE);
			return;
		}
		
			// (16 blocks per chunk * 32 chunks per region)^2 * 2 bytes per coord = 524288
			byte[] data = new byte[524288];
			byte[] coords = new byte[2];
			
			printm("Calculating biome values..."+NEW_LINE);
			
			if (!(outputDir.exists() && outputDir.isDirectory()))
			{	
				if (!outputDir.mkdirs())
				{
					printe("No write access to world folder. Cannot continue."+NEW_LINE);
					return;
				}
			}
			
			printm("Scanning save folder..." + NEW_LINE);
			
			this.setupWorldBounds(worldFolder);
			printm("World Size: "+ Integer.toString(chunks.size()) + " regions" + NEW_LINE);
			printm("Saving biome data...  (press esc to cancel)" + NEW_LINE);
			
			for (int x = 0; x<chunks.size();x++)
			{		
					final int fromX = chunks.get(x)[0]*512;
					final int toX = (chunks.get(x)[0]+1)*512;
					final int fromZ = chunks.get(x)[1]*512;
					final int toZ = (chunks.get(x)[1]+1)*512;
					
					File biomeFile = new File(outputDir, "b." + Integer.toString(chunks.get(x)[0])+"."+Integer.toString(chunks.get(x)[1])+".biome");
					
					if (!biomeFile.exists())
					{
						for(int i = fromX; i < toX; i++)
						{
							for(int j = fromZ; j < toZ; j++)
							{	
								coords = getCoordsAtBlock(i,j);
								data[2*(i-fromX)+(j-fromZ)*1024] = coords[1];
								data[2*(i-fromX)+1+(j-fromZ)*1024] = coords[0];
							}
						}
					
						// Write out the biome data here.
			            FileOutputStream fileoutputstream;
						try
						{
							fileoutputstream = new FileOutputStream(biomeFile);
							fileoutputstream.write(data, 0, 524288);
				            fileoutputstream.close();
						}
						catch (Throwable e)
						{
							printe("A biome file failed to write. Stopping."+NEW_LINE);
							return;
						}
					}
					
					// Progress Report (so the user doesn't get too anxious, this can take forever)
					
					int percent = (int)(((float)x/(float)chunks.size())*100);
					if (percent > 100)
						percent = 100;
					if (percent-lastPercent >= 1)
					{
						if (countPercents%10 == 0)
							printm(NEW_LINE);
						printm(Integer.toString(percent)+"% ... ");
						
						lastPercent = percent;
						countPercents++;
					}
					if (die)
						break;
			}
			if (!die)
			{
				printm(NEW_LINE+NEW_LINE+"Done! This world is now ready to be used with a biome-capable"+NEW_LINE);
				printm("mapping program such as mcmap. If your boundaries expand, you will need"+NEW_LINE);
				printm("to come back and re-process. (It will be much faster the second time)"+NEW_LINE+NEW_LINE);
				printm("You may now exit or select another world to process."+NEW_LINE+NEW_LINE);
			}
			else
			{
				printm(NEW_LINE+"Operation safely cancelled (progress saved)."+NEW_LINE+NEW_LINE);
				printm("You may now exit or select another world to process."+NEW_LINE+NEW_LINE);
			}
		}
	
	private void printm(final String msg)
	{
		if (!errorsOnly)
		{
			if (useGUI)
				gui.message(msg);
			else
				System.out.print(msg);
		}
	}
	
	private void printe(final String msg)
	{
		if (useGUI)
			gui.message(msg);
		else
			System.out.print(msg);
	}
	
	public boolean loadWorld()
	{
		// Save Handler Arguments
		//File paramFile, String paramString, boolean paramBoolean
		Object handlerArgs[] = new Object[3];
		handlerArgs[0] = worldFolder;
		handlerArgs[1] = "";
		handlerArgs[2] = false;
        
		try
		{
			saveHandler = handleMinecraftSave.newInstance(handlerArgs);
		}
		catch (IllegalArgumentException e1)
		{
			printe("Minecraft Save handler rejected arguments!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InstantiationException e1)
		{
			printe("Minecraft Save handler failed to instantiate!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (IllegalAccessException e1)
		{
			printe("Minecraft Save handler missing!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InvocationTargetException e1)
		{
			printe("Minecraft Save handler invocation failed!"+NEW_LINE);
			printe("Minecraft version was incompatible."+NEW_LINE);
			return false;
		}
		
		
		// Creating the Minecraft Save object
		//rw paramrw, String paramString, long paramLong)
		Object saveArgs[] = new Object[3];
        saveArgs[0] = saveHandler;
        saveArgs[1] = "";
        saveArgs[2] = 0;
        
		Object saveArgs2[] = new Object[4];
        saveArgs2[0] = saveHandler;
        saveArgs2[1] = "";
        saveArgs2[2] = 0L;
        saveArgs2[3] = null;
      
		try
		{
			if (isServerJar)
				minecraftSave = createMinecraftSave.newInstance(saveArgs2);
			else
				minecraftSave = createMinecraftSave.newInstance(saveArgs);
		}
		catch (IllegalArgumentException e1)
		{
			printe("Minecraft Save object rejected arguments!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InstantiationException e1)
		{
			printe("Minecraft Save object failed to instantiate!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (IllegalAccessException e1)
		{
			printe("Minecraft Save object missing!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InvocationTargetException e1)
		{
			printe("Minecraft Save object invocation failed!"+NEW_LINE);
			printe("Minecraft version was incompatible."+NEW_LINE);
			return false;
		}
		
		if (loadedField != null)
		{
			try
			{
				if (!loadedField.getBoolean(minecraftSave))
				{
					printm("Level Loaded!"+NEW_LINE);
				}
				else
				{
					printe("Level failed to load!"+NEW_LINE);
					return false;
				}
			}
			catch (IllegalArgumentException e2)
			{
				printe("Couldn't check if save loaded."+NEW_LINE);
				printm("Things might not work."+NEW_LINE);
			}
			catch (IllegalAccessException e2)
			{
				printe("Couldn't check if save loaded."+NEW_LINE);
				printm("Things might not work."+NEW_LINE);
			}
		}
	
		// Creating the Biome Generator using the minecraft save object
		Object bioGenArgs[] = new Object[1];
		bioGenArgs[0] = minecraftSave;
		try
		{
			biomeGenerator = createBiomeGenerator.newInstance(bioGenArgs);
		}
		catch (IllegalArgumentException e1)
		{
			printe("BiomeGen constructor rejected arguments!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InstantiationException e1)
		{
			printe("BiomeGen object failed to instantiate!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (IllegalAccessException e1)
		{
			printe("BiomeGen object missing!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		catch (InvocationTargetException e1)
		{
			printe("BiomeGen object invocation failed!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		return true;
	}

	// Setup the chunks for the currently selected world
	private void setupWorldBounds(File worldFolder)
	{
		chunks = new ArrayList<int[]>();
		String[] parts;
		File regionFolder = new File(worldFolder,"region");
		if (regionFolder.isDirectory())
		{
			File[] potentialChunks = regionFolder.listFiles();
			for(int i=0;i<potentialChunks.length;i++)
			{
				parts = potentialChunks[i].getName().split("\\.");
				if (parts.length==4 && parts[0].equals("r") && parts[3].equals("mcr"))
				{
					int[] mcrChunk = new int[2];
					mcrChunk[0] = Integer.parseInt(parts[1]);
					mcrChunk[1] = Integer.parseInt(parts[2]);
					chunks.add(mcrChunk);
				}
			}
		}
	}
	
    
    
    // If no EXTRACTEDBIOMES folder exists, make one.
    // Copy in grasscolor.png and foliagecolor.png
    private void setupDataFolder(final File biomesFolder)
    {
		if (!(biomesFolder.exists() && biomesFolder.isDirectory()))
		{	
			if (!biomesFolder.mkdir())
			{
				printm("No write access to world folder. Cannot continue."+NEW_LINE);
				
				return;
			}
		}
		
		try
		{
			if (grassColourImage != null)
			{
				ImageIO.write(grassColourImage, "png", new File(biomesFolder,"grasscolor.png"));
			}
			if (foliageColourImage != null)
			{
				ImageIO.write(foliageColourImage, "png", new File(biomesFolder,"foliagecolor.png"));
			}
		}
		catch (IOException e)
		{
			printm("Failed to write biome color pngs. Continuing..."+NEW_LINE);
		}
    }
    
    // Given a signature that came out of generateSignatures at some point in the past
    // find the best match of the classes loaded from the minecraft.jar.
    private int matchClassSignature(String signature)
    {
    	String[] sig_components=null;
    	String[] ref_components=null;
    	int peak_matches = 1;
    	int peak_index = -1;
    	int i;
    	for (i = 0; i<class_signatures.size();i++)
    	{
    		int matches = 0;
    		int antimatches = 0;
    		sig_components = class_signatures.get(i).split("\\+");
    		ref_components = signature.split("\\+");
    		
    		for (int j=0;j<ref_components.length;j++)
    		{
    			for (int k=0;k<sig_components.length;k++)
    			{
    				if (ref_components[j].equalsIgnoreCase(sig_components[k]))
    				{
    					sig_components[k]="";
    					matches++;
    					break;
    				}
    			}
    		}
    		
    		sig_components = class_signatures.get(i).split("\\+");
    		
    		boolean matched;
    		for (int j=0;j<sig_components.length;j++)
    		{
    			matched = false;
    			for (int k=0;k<ref_components.length;k++)
    			{
    				if (ref_components[k].equalsIgnoreCase(sig_components[j]))
    				{
    					ref_components[k] = "";
    					matched = true;
    					break;
    				}
    			}
    			if (!matched)
    				antimatches++;
    		}
    		
			if ((matches-antimatches) > peak_matches)
			{
				peak_matches = matches-antimatches;
				peak_index = i;
			}
    	}
    	Long ms = Math.round(100.0*(float)peak_matches/(float)ref_components.length);
    	printm("Match strength: " + ms.toString() + "%\t");
		
    	return peak_index;
    }
    
	  
	/*
	 * The manual API
	 */
	  public double getTemperatureAtBlock(final int x, final int z) // Returns the temperature (double)
	  {
		  	argList[0] = x;
			argList[1] = z;
			try
			{
				generateForLoaction.invoke(biomeGenerator, argList);  // BiomeGenerator.a(i,j,1,1);
			}
			catch (Throwable e1)
			{
				printe("Could not generate biome vals for coords..."+NEW_LINE);
			} 
			
			try
			{
				return ((double[])genTemp.get(biomeGenerator))[0]; // BiomeGenerator.a[0];
			}
			catch (Throwable e)
			{
				printe("Could not extract temp from generator"+NEW_LINE);
				return 0;
			}
	  }
	  
	  public double getMoistureAtBlock(final int x, final int z) // Returns the moisture (double)
	  {
		  	argList[0] = x;
			argList[1] = z;
			try
			{
				generateForLoaction.invoke(biomeGenerator, argList);  // BiomeGenerator.a(i,j,1,1);
			}
			catch (Throwable e1)
			{
				printe("Could not generate biome vals for coords..."+NEW_LINE);
			} 
			
			try
			{
				return ((double[])genMoist.get(biomeGenerator))[0]; // BiomeGenerator.b[0];
			}
			catch (Throwable e)
			{
				printe("Could not extract moist from generator"+NEW_LINE);
				return 0;
			}
	  }

	public byte[] getCoordsAtBlock(final int x, final int z) // Returns the location of the biome color in the 256x256 biome PNG (an int)
	{
		if (biomeGenerator == null)
			throw new NullPointerException("BiomeGenerator is null!");
		
		argList[0] = x;
		argList[1] = z;
			byte[] coords = new byte[2];
			coords[0] = 0;
			coords[1] = 0;
			try
			{
				generateForLoaction.invoke(biomeGenerator, argList);  // BiomeGenerator.a(i,j,1,1);
			}
			catch (Throwable e1)
			{
				printe("Could not generate biome vals for coords..."+NEW_LINE);
			} 
			double temp, moisture;
			try
			{
				temp = ((double[])genTemp.get(biomeGenerator))[0]; // BiomeGenerator.a[0];
				moisture = ((double[])genMoist.get(biomeGenerator))[0]; // BiomeGenerator.b[0];
			}
			catch (Throwable e)
			{
				printe("Could not extract temp/moist from generator"+NEW_LINE);
				return coords;
			}
			
			// Reconstruct the double-to-int function here
			moisture *= temp;
			coords[0] = (byte) ((1.0D - temp) * 255.0D);
			coords[1] = (byte) ((1.0D - moisture) * 255.0D);
			return coords;
	  }
	  
	  // Returns the biome color at a given block, packed in an int as RGB
	  public int getRGBAtBlock(final int x, final int z, final ColourType type)
	  {
		  	final byte[] coords = getCoordsAtBlock(x,z);
		  	
			if (type == ColourType.GrassColour)
				return grassColourImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else if (type == ColourType.FoliageColour)
				return foliageColourImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else
				return 0;
	  }
	  
	// Returns the biome color at a given block
	public Color getColorAtBlock(final int x, final int z, final ColourType type)
	{
		return new Color(getRGBAtBlock(x, z, type));
	}
	  
	public boolean setBiomeImages(final File grasscolor, final File foliagecolor)
	{
			try 
			{
				if (grasscolor != null)
				{
					grassColourImage = ImageIO.read(grasscolor);
				}
				if (foliagecolor != null)
				{
					foliageColourImage = ImageIO.read(foliagecolor);
				}
			} 
			catch (IOException e) 
			{
				return false;
			}
		useDefaultBiomeImages = false;
		return true;
	}
	
	public void setJarLocation(File mjar)
	{
		minecraftJar = mjar;
	}
	
	public boolean bindToMinecraft()
	{
		// This method kind of crazy. 
		// 1.) Find the minecraft.jar
		// 2.) Check for MOJANG signatures
		//		- If they exist rewrite the zip to not include them
		// 3.) Take a note of all files ending in .class
		// 4.) Copy grasscolor and foliagecolor to memory.
		// 5.) Close the zip file
		// 6.) Add minecraft.jar to the classpath
		// 7.) Scan through and match the save class and biome classes
		//		based on signatures
		// 8.) Bind the classes and functions we need to reflection variables
		
		// Auto find minecraft jar if we don't have one explicitly set
		if (minecraftJar == null)
		{
			printm("Discovering minecraft.jar interface..."+NEW_LINE);
			minecraftJar = MinecraftUtils.findMinecraftJar( MinecraftUtils.findMinecraftDir() );
		}
		
		// Check minecraft jar looks valid
		if (minecraftJar == null || !minecraftJar.exists())
		{
			printe("Failed to locate minecraft.jar"+NEW_LINE);
			printe("Path: " + minecraftJar.getAbsolutePath() +NEW_LINE);
			printe("Minecraft doesn't appear to be installed!"+NEW_LINE);

			return false;
		}
		
		classListing = new ArrayList<String>();
		try
		{
			// Take a copy of the jar to work on and strip out the signature while we're at it
			File newMinecraftJar = new File(minecraftJar.getParentFile(), minecraftJar.getName()+".new");
			stripMinecraftJar(minecraftJar, newMinecraftJar);
			minecraftJar = newMinecraftJar;
			
			// Now we've got a copy we can go to work on it
			
			ZipFile mcjar = new ZipFile(minecraftJar);
			
			// Copy grasscolor.png and foliagecolor.png
			ZipEntry grasscolor = mcjar.getEntry("misc/grasscolor.png");
			ZipEntry foliagecolor = mcjar.getEntry("misc/foliagecolor.png");
			
			// TODO: This doesn't close the input streams properly

			// Only try to grab the biome color images from minecraft if they exist in the JAR
			// and they are not already loaded (from the setBiomeImages method, for example).
			if (grasscolor != null && useDefaultBiomeImages)
			{
				grassColourImage = ImageIO.read(mcjar.getInputStream(grasscolor));
			}
			if (foliagecolor != null && useDefaultBiomeImages)
			{
				foliageColourImage = ImageIO.read(mcjar.getInputStream(foliagecolor));
			}
			
			// Scan the jar file and gather a class listing
			
			Enumeration<? extends ZipEntry> e = mcjar.entries();
			while (e.hasMoreElements())
			{
				ZipEntry currentFile = e.nextElement();
				String[] components = currentFile.getName().split("/");
				String fileName = components[components.length-1];
				
				if (fileName.toLowerCase().endsWith(".class") && currentFile.getName().length() > 5)
				{
					String className = fileName.substring(0, fileName.length()-6);
					classListing.add(className);
				}
			}
			
			mcjar.close();
			
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
            printe("Failed to open new jar for writing!"+NEW_LINE);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			printe("Exception while trying to open jar file"+NEW_LINE);
		}
	
		
		try
		{
			ClasspathUtil.addJarToClasspath(minecraftJar);
		}
		catch (IOException e)
		{
			printe("I can't seem to find your minecraft.jar file!"+NEW_LINE);
		}
		
		// The classes, methods, and fields we need, as strings:
		// Classes
		String abstract_save_handler_class = "ec";
		String abstract_save_handler_class_signature = ABSTRACT_SAVE_HANDLER_REF;
		
		String save_handler_class = "ec";
		String save_handler_class_signature = SAVE_HANDLER_CLASS_REF;
		
		String save_class = "cu";
		String save_class_signature = SAVE_CLASS_REF;
		
		String biome_gen_class = "pb";
		String biome_gen_class_signature = BIOME_GEN_CLASS_REF;
		
		// Methods
		// This is unlikely to change so I don't need to detect it.
		String biome_generator = "a";
		
		//Fields
		// Typically the not loaded flag is the 3rd boolean in the save class
		String save_notloaded = "q";
		String save_notloaded_type = "boolean";
		int save_notloaded_count = 3;
		
		// I doubt these will change. I'll deal with it later.
		String biome_gen_temp = "a";
		String biome_gen_moist = "b";
		
		class_signatures = new ArrayList<String>();
		
		for (int i = 0; i<classListing.size();i++)
		{
			class_signatures.add(ReflectionUtil.generateClassSignature(classListing.get(i)));
			
			// Dump all the class signatures - helpful!
			if(printClassRefStrings)
			{
				System.out.println(classListing.get(i));
				System.out.println(ReflectionUtil.generateClassSignature(classListing.get(i)));
				System.out.println("\n");
			}
		}
		
		int class_id = this.matchClassSignature(abstract_save_handler_class_signature);
		if (class_id != -1)
		{
			abstract_save_handler_class = classListing.get(class_id);
			printm("Handler interface is: "+abstract_save_handler_class+NEW_LINE);
			if (printClassRefStrings)
				printm(class_signatures.get(class_id)+NEW_LINE);
		}
		else
		{
			printe(NEW_LINE + "Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for handler class not found."+NEW_LINE);
			printe("Class listing ("+ Integer.toString(classListing.size()) + "entires)" + NEW_LINE);
			
			for (int i=0; i<classListing.size(); i++)
			{
				printe("\t"+ classListing.get(i) + ".class" + NEW_LINE);
			}
			return false;
		}
		
		class_id = this.matchClassSignature(save_handler_class_signature);
		if (class_id != -1)
		{
			save_handler_class = classListing.get(class_id);
			printm("Handler class is: "+save_handler_class+NEW_LINE);
			if (printClassRefStrings)
				printm(class_signatures.get(class_id)+NEW_LINE);
		}
		else
		{
			printe(NEW_LINE + "Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for save class not found."+NEW_LINE);
			printe("Class listing ("+ Integer.toString(classListing.size()) + "entires)" + NEW_LINE);
			
			for (int i=0; i<classListing.size(); i++)
			{
				printe("\t"+ classListing.get(i) + ".class" + NEW_LINE);
			}
			return false;
		}
		
		
		class_id = this.matchClassSignature(save_class_signature);
		if (class_id != -1)
		{
			save_class = classListing.get(class_id);
			printm("Save class is: "+save_class+NEW_LINE);
			if (printClassRefStrings)
				printm(class_signatures.get(class_id)+NEW_LINE);
		}
		else
		{
			printe(NEW_LINE + "Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for save class not found."+NEW_LINE);
			printe("Class listing ("+ Integer.toString(classListing.size()) + "entires)" + NEW_LINE);
			
			for (int i=0; i<classListing.size(); i++)
			{
				printe("\t"+ classListing.get(i) + ".class" + NEW_LINE);
			}
			return false;
		}
		
		class_id = this.matchClassSignature(biome_gen_class_signature);
		if (class_id != -1)
		{
			biome_gen_class = classListing.get(class_id);
			printm("Biome Gen class is: "+biome_gen_class+NEW_LINE);
			if (printClassRefStrings)
				printm(class_signatures.get(class_id)+NEW_LINE);
		}
		else
		{
			printe("Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for biome gen class not found."+NEW_LINE);
			return false;
		}
	
		// Get the classes we need using reflection
		
		try
		{
			saveHandlerClass = Class.forName(save_handler_class);
			minecraftSaveClass = Class.forName(save_class);
			biomeGeneratorClass = Class.forName(biome_gen_class);
			abstractSaveHandlerClass = Class.forName(abstract_save_handler_class);
		}
		catch (ClassNotFoundException e2)
		{
			printe("Can't find Minecraft! Looked here:"+NEW_LINE);
			printe(minecraftJar.getAbsolutePath()+NEW_LINE);
			printe("Make certain minecraft.jar is in that location."+NEW_LINE);
			return false;
		}
		
		Class<?> partypes[] = new Class[3];
        partypes[0] = File.class;
        partypes[1] = String.class;
        partypes[2] = Boolean.TYPE;
        
		try
		{
			handleMinecraftSave = saveHandlerClass.getConstructor(partypes);
		}
		catch (SecurityException e1)
		{
			printe("Could not bind MinecraftSaveHandler Constructor (security issue)"+NEW_LINE);
			printe("I'm trying to delete the Mojang signatures for you."+NEW_LINE);
			printe("Restart the program and try again."+NEW_LINE);
			return false;
		}
		catch (NoSuchMethodException e1)
		{
			// Save handler failed to bind
			printe("Could not bind MinecraftSaveHandler Constructor."+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		
		
		// Setup the MINECRAFTSAVECLASS constructor
		// Client
		partypes = new Class[3];
        partypes[0] = abstractSaveHandlerClass;
        partypes[1] = String.class;
        partypes[2] = Long.TYPE;
        
		try
		{
			createMinecraftSave = minecraftSaveClass.getConstructor(partypes);
		}
		catch (SecurityException e1)
		{
			printe("Could not bind MinecraftSave Constructor (security issue)"+NEW_LINE);
			printe("I'm trying to delete the Mojang signatures for you."+NEW_LINE);
			printe("Restart the program and try again."+NEW_LINE);
			return false;
		}
		catch (NoSuchMethodException e1)
		{
			// This could mean that you are operating on a server JAR. Try this:
			try
			{
				printm("Server software detected."+NEW_LINE);
				String server_random_class_name = classListing.get(this.matchClassSignature(SERVER_RAND_CLASS_REF));
				printm("Server random class is: " + server_random_class_name + NEW_LINE);
				serverRandomClass = Class.forName(server_random_class_name);
				
				// Server constructors look like this:
				//   public cp(mi parammi, String paramString, long paramLong, mn parammn)
				
				partypes = new Class[4];
		        partypes[0] = abstractSaveHandlerClass;
		        partypes[1] = String.class;
		        partypes[2] = Long.TYPE;
				partypes[3] = serverRandomClass;
				createMinecraftSave = minecraftSaveClass.getConstructor(partypes);
				isServerJar = true;
				
				if (printClassRefStrings)
					printm(class_signatures.get(this.matchClassSignature(SERVER_RAND_CLASS_REF))+NEW_LINE);
			}
			catch (Throwable e)
			{
				printe("Could not bind MinecraftSave Constructor for servers."+NEW_LINE);
				printe("Minecraft version was incompatible"+NEW_LINE);
				return false;
			}
		}
		
        // Setup the BIOMEGENERATORCLASS constructor
		partypes = new Class[1];
        partypes[0] = minecraftSaveClass;
        try {
			createBiomeGenerator = biomeGeneratorClass.getConstructor(partypes);
		} catch (SecurityException e1) {
			printe("Could not bind BiomeGenerator Constructor (security issue)"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		} catch (NoSuchMethodException e1) {
			printe("Could not bind BiomeGenerator Constructor"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		
		// Setup the save class loaded field
		save_notloaded = ReflectionUtil.getFieldWithType(save_class, save_notloaded_type, save_notloaded_count);

		try {
			loadedField = minecraftSaveClass.getField(save_notloaded);
		} catch (SecurityException e2) {
			printe("Couldn't check if save loaded."+NEW_LINE);
			printm("Trying anyway..."+NEW_LINE);
		} catch (NoSuchFieldException e2) {
			printe("Couldn't check if save loaded."+NEW_LINE);
			printm("Trying anyway..."+NEW_LINE);
		}
		
		 // Binding the method that generates the biomes
        // Setup the parameters the methods takes
        partypes = new Class[4];
        partypes[0] = Integer.TYPE;
        partypes[1] = Integer.TYPE;
        partypes[2] = Integer.TYPE;
        partypes[3] = Integer.TYPE;
        
        // Create the method object
		try {
			generateForLoaction = biomeGeneratorClass.getMethod(biome_generator, partypes);
		} catch (SecurityException e1) {
			printe("Failed to setup biogen method!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		} catch (NoSuchMethodException e1) {
			printe("Failed to setup biogen method!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		
        // Binding the fields that read out the temperature and moisture
		try {
			genTemp = biomeGeneratorClass.getField(biome_gen_temp);
			genMoist = biomeGeneratorClass.getField(biome_gen_moist);
		} catch (SecurityException e1) {
			printe("Failed to setup temp/moist fields!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		} catch (NoSuchFieldException e1) {
			printe("Failed to setup temp/moist fields!"+NEW_LINE);
			printe("Minecraft version was incompatible"+NEW_LINE);
			return false;
		}
		printm("Ready!"+NEW_LINE+NEW_LINE);
		
		bound = true;
		return true;
	}
	
	private static void stripMinecraftJar(File srcJar, File destJar) throws Exception
	{
		// Copy the jar and strip out the signature while we're at it
		
		FileOutputStream dest = null;
		ZipOutputStream zout = null;
		
		try
		{
			byte data[] = new byte[1024 * 2];
			
			dest = new FileOutputStream(destJar);
			zout = new ZipOutputStream(new BufferedOutputStream(dest));
			
			ZipFile mcjar = new ZipFile(srcJar);
			Enumeration<? extends ZipEntry> e = mcjar.entries();
			while(e.hasMoreElements())
			{
				ZipEntry currentfile = (ZipEntry)e.nextElement();
	
				// Only copy files which don't end in .dsa or .sf
				if ( !(currentfile.getName().endsWith(".DSA") || currentfile.getName().endsWith(".SF")) )
				{
					BufferedInputStream is = new BufferedInputStream(mcjar.getInputStream(currentfile));
					zout.putNextEntry(currentfile);
					
					int count = 0;
		            while((count = is.read(data, 0, data.length)) != -1)
		            {
		               zout.write(data, 0, count);
		            }
		            is.close();
				}
				else
				{
					System.out.println("Skipping "+currentfile.getName());
				}
			}
		}
		finally
		{
			try
			{
				if (zout != null)
					zout.close();
			}
			catch (Exception e) {}
			try
			{
				if (dest != null)
					dest.close();
			}
			catch (Exception e) {}
		}
	}
	
	// END CLASS DEF
}
