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
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.filechooser.FileSystemView;

public class WorldProcessor implements Runnable
{
	
	private static final String NEW_LINE = "\n";
	
	private static final String SAVE_CLASS_REF = "carg:class java.io.File carg:class java.lang.String +carg:class java.io.File carg:class java.lang.String carg:long +carg:class java.lang.String carg:class ox carg:long +carg:class cx carg:class ox +carg:class java.io.File carg:class java.lang.String carg:long carg:class ox +ret:void+arg:int arg:int arg:int ret:void+ret:void+arg:float ret:class+arg:int arg:int ret:class+arg:int arg:int arg:int arg:int ret:boolean+arg:class ret:boolean+arg:class ret:void+ret:void+arg:int arg:int arg:int ret:float+arg:int arg:int arg:int arg:class ret:void+arg:class arg:class ret:interface java.util.List+arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:class ret:boolean+arg:class arg:int arg:int arg:int arg:int ret:void+arg:class ret:boolean+arg:class arg:class ret:void+arg:int arg:int arg:int ret:class+arg:int arg:int ret:class+arg:class ret:void+arg:interface ju ret:void+ret:void+arg:float ret:float+arg:interface java.util.List ret:void+arg:int arg:int arg:int arg:int ret:void+arg:class ret:int+arg:int arg:int arg:int arg:int arg:int ret:boolean+ret:boolean+arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:boolean+arg:class arg:int arg:int arg:int arg:float ret:class+arg:class ret:void+ret:class+arg:class ret:interface bd+arg:int arg:int ret:int+arg:class ret:void+arg:boolean arg:interface ps ret:void+arg:int ret:boolean+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:boolean ret:int+arg:class arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int ret:int+arg:class arg:class ret:class+arg:class arg:class arg:boolean ret:class+arg:class arg:class arg:float arg:float ret:void+arg:double arg:double arg:double arg:class arg:float arg:float ret:void+arg:class arg:int arg:int arg:int ret:void+arg:class arg:double arg:double arg:double arg:double arg:double arg:double ret:void+arg:class ret:boolean+arg:interface ju ret:void+arg:class arg:class ret:interface java.util.List+arg:float ret:int+arg:class arg:float ret:class+arg:class arg:boolean ret:void+arg:class ret:boolean+arg:class arg:class arg:class ret:boolean+arg:class arg:class ret:boolean+arg:class arg:double arg:double arg:double arg:float ret:void+arg:class arg:class ret:float+arg:class ret:class+arg:int arg:int arg:int arg:class ret:void+arg:interface ps ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int arg:boolean ret:void+arg:boolean ret:boolean+arg:class arg:class ret:interface java.util.List+arg:interface java.util.List ret:void+arg:int arg:int arg:int arg:int arg:boolean ret:boolean+arg:class arg:class arg:float ret:class+arg:class arg:class ret:class+arg:class arg:double ret:class+arg:double arg:double arg:double arg:double ret:class+arg:int arg:int arg:int arg:int arg:int arg:int arg:class ret:void+arg:long ret:void+arg:class arg:int arg:int arg:int ret:boolean+arg:class arg:byte ret:void+arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int ret:void+arg:int arg:int arg:int arg:int ret:void+ret:void+arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int arg:int ret:void+arg:class ret:void+ret:class+arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:class+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:boolean+ret:void+ret:interface java.util.List+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int ret:boolean+ret:void+arg:class ret:void+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int ret:int+arg:float ret:float+ret:void+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:boolean+arg:class ret:void+arg:int arg:int ret:int+ret:boolean+arg:int arg:int arg:int arg:int ret:boolean+arg:float ret:class+arg:int arg:int arg:int ret:boolean+ret:void+fld:boolean+fld:interface java.util.List+fld:interface java.util.List+fld:interface java.util.List+fld:class+fld:interface java.util.Set+fld:interface java.util.List+fld:interface java.util.List+fld:long+fld:long+fld:int+fld:int+fld:int+fld:boolean+fld:long+fld:int+fld:int+fld:class+fld:int+fld:int+fld:int+fld:boolean+fld:class+fld:interface java.util.List+fld:interface bd+fld:class+fld:class+fld:long+fld:class+fld:long+fld:class+fld:boolean+fld:class+fld:int+fld:int+fld:interface java.util.Set+fld:int+fld:interface java.util.List+fld:boolean+";
	private static final String BIOME_GEN_CLASS_REF = "+carg:class cx +arg:int arg:int ret:double+arg:int arg:int ret:class+arg:class ret:class+arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+";
	private static final String SERVER_RAND_CLASS_REF = "+ret:interface cg+ret:void+ret:void+arg:class ret:void+arg:int arg:int ret:boolean+arg:long arg:float ret:float+arg:int ret:class+arg:class ret:interface bd+fld:class+fld:class+fld:boolean+fld:boolean+fld:boolean+fld:class+fld:int+fld:class+";
	
	private MinecraftBiomeExtractorGUI gui = null;
	
	// Minecraft Class Bindings
	private File minecraftJar = null;
	private ArrayList<String> class_listing;
	private ArrayList<String> class_signatures;
	private boolean useGUI = true;
	private Class<?> minecraftSaveClass;
	private Class<?> biomeGeneratorClass;
	private Constructor<?> createMinecraftSave;
	private Constructor<?> createBiomeGenerator;
	private Method generateForLoaction;
    private Field loadedField = null;
    private Field genTemp;
    private Field genMoist;
    private Object minecraftSave;
    private Object biomeGenerator;
	private Object argList[]; // Used to call the biome generator.
    
    // Storage for the grasscolor and foliagecolor pngs
	private BufferedImage grassColourImage = null;
	private BufferedImage foliageColourImage = null;
	
	// World Variables
	private File worldFolder = null;
	private int chunkMinX = 0;
	private int chunkMaxX = 0;
	private int chunkMinZ = 0;
	private int chunkMaxZ = 0;
	
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
		chunkMinX = 0;
		chunkMaxX = 0;
		chunkMinZ = 0;
		chunkMaxZ = 0;
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
			printe("No output dir specified\n");
			return;
		}
		
		if (outputDir.exists() && flush)
		{
			printm("Deleting existing biome data in " + outputDir.getAbsolutePath() + NEW_LINE);
			FileUtils.deleteDirectory(outputDir);
		}
	
		printm("Opening " + worldFolder.getName() + "..." + NEW_LINE);
		this.setupDataFolder(worldFolder.getAbsolutePath());

		printm("Locating Minecraft save..."+NEW_LINE);
		if (!this.loadWorld())
		{
			printe("Minecraft classes failed to load the world."+ NEW_LINE);
			return;
		}
			
			byte[] data = new byte[32768];
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
			
			this.setupWorldBounds(worldFolder, 0);
			printm("World Bounds: ( "+ Integer.toString(chunkMinX) + " , " + Integer.toString(chunkMinZ) + " ) to ( " + Integer.toString(chunkMaxX) + " , " + Integer.toString(chunkMaxZ) + " )" + NEW_LINE);
			printm("Saving biome data...  (press esc to cancel)" + NEW_LINE);
			
			for (int x = (chunkMinX/8-1)*8; x<=((chunkMaxX/8)*8); x=x+8)
			{
				for (int z = (chunkMinZ/8-1)*8; z<=((chunkMaxZ/8)*8); z=z+8)
				{
					
					final int fromX = x*16;
					final int toX = (x+8)*16;
					final int fromZ = z*16;
					final int toZ = (z+8)*16;
					
					File biomeFile = new File(outputDir, Integer.toString(x)+"."+Integer.toString(z)+".biome");
					
					if (!biomeFile.exists())
					{
						for(int i = fromX; i < toX; i++)
						{
							for(int j = fromZ; j < toZ; j++)
							{	
								coords = getCoordsAtBlock(i,j);
								data[2*(i-fromX)+(j-fromZ)*256] = coords[1];
								data[2*(i-fromX)+1+(j-fromZ)*256] = coords[0];
							}
						}
					
						// Write out the biome data here.
			            FileOutputStream fileoutputstream;
						try
						{
							fileoutputstream = new FileOutputStream(biomeFile);
							fileoutputstream.write(data, 0, 32768);
				            fileoutputstream.close();
						}
						catch (Throwable e)
						{
							printe("A biome file failed to write. Stopping."+NEW_LINE);
							return;
						}
					}
					
					// Progress Report (so the user doesn't get too anxious, this can take forever)
					final int iteration = (z-chunkMinZ) + (x-chunkMinX) * (chunkMaxZ-chunkMinZ);
					final int totalIteration = (chunkMaxX-chunkMinX)*(chunkMaxZ-chunkMinZ);
					int percent = (int)(((float)iteration/(float)totalIteration)*100);
					if (percent > 100)
						percent = 100;
					if (percent-lastPercent >= 1)
					{
						if (countPercents%10 == 0)
							printm(NEW_LINE);
						printm(Integer.toString((int)(((float)iteration/(float)totalIteration)*100))+"% ... ");
						
						lastPercent = (int)(((float)iteration/(float)totalIteration)*100);
						countPercents++;
					}
					if (die)
						break;
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
		// Creating the Minecraft Save object
		Object saveArgs[] = new Object[2];
        saveArgs[0] = worldFolder;
        saveArgs[1] = "";
        
		Object saveArgs2[] = new Object[4];
        saveArgs2[0] = worldFolder;
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

	// Setup the min/max chunk values for the currently selected world
	private void setupWorldBounds(File start, int level)
	{
		level++;
		if (level > 3)
			return;
		File[] listing = start.listFiles();
		String[] parts;
		for(int i=0; i<listing.length; i++)
		{
			if (listing[i].isDirectory() && !listing[i].getName().startsWith(".") && !listing[i].getName().equalsIgnoreCase("DIM-1"))
			{
				setupWorldBounds(listing[i],level);
			}
			else if (listing[i].getName().endsWith(".dat") && level==3)
			{
				// Decode X,Z and place in max,mins
				parts = listing[i].getName().split("\\.");
				if (parts.length>2)
				{
					final int x = Integer.parseInt(parts[1], 36);
					final int z = Integer.parseInt(parts[2], 36);
					
					if (Math.abs(x) < 4096 && Math.abs(z) < 4096)
					{
						if (x<chunkMinX)
							chunkMinX = x;
						if (x>chunkMaxX)
							chunkMaxX = x;
						if (z<chunkMinZ)
							chunkMinZ = z;
						if (z>chunkMaxZ)
							chunkMaxZ = z;
					}
				}
			}
		}
	}
	
    
    
    // If no EXTRACTEDBIOMES folder exists, make one.
    // Copy in grasscolor.png and foliagecolor.png
    private void setupDataFolder(String worldpath)
    {
    	File biomesFolder = new File(worldpath,"EXTRACTEDBIOMES");
		if (!(biomesFolder.exists() && biomesFolder.isDirectory()))
		{	
			if (!biomesFolder.mkdir())
			{
				printm("No write access to world folder. Cannot continue."+NEW_LINE);
				
				return;
			}
		}
		
		try {
			if (grassColourImage != null)
			{
				ImageIO.write(grassColourImage, "png", new File(biomesFolder,"grasscolor.png"));
			}
			if (foliageColourImage != null)
			{
				ImageIO.write(foliageColourImage, "png", new File(biomesFolder,"foliagecolor.png"));
			}
		} catch (IOException e) {
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
		  	argList[0] = x;
			argList[1] = z;
			byte[] coords = new byte[2];
			coords[0] = 0;
			coords[1] = 0;
			try {
				generateForLoaction.invoke(biomeGenerator, argList);  // BiomeGenerator.a(i,j,1,1);
			} catch (Throwable e1) {
				printe("Could not generate biome vals for coords..."+NEW_LINE);
			} 
			double temp,moisture;
			try {
				temp = ((double[])genTemp.get(biomeGenerator))[0]; // BiomeGenerator.a[0];
				moisture = ((double[])genMoist.get(biomeGenerator))[0]; // BiomeGenerator.b[0];
			} catch (Throwable e) {
				printe("Could not extract temp/moist from generator"+NEW_LINE);
				return coords;
			}
			
			// Reconstruct the double-to-int function here
			moisture *= temp;
			coords[0] = (byte) ((1.0D - temp) * 255.0D);
			coords[1] = (byte) ((1.0D - moisture) * 255.0D);
			return coords;
	  }
	  public int getRGBAtBlock(final int x, final int z, final int type) // Returns a Color (or an int for efficiency) of the biome color at a given block
	  {
		  	byte[] coords = getCoordsAtBlock(x,z);
		  	//System.out.print("("+Byte.toString(coords[0])+","+Byte.toString(coords[1])+")");
			if (type == 0)
				return grassColourImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else if (type == 1)
				return foliageColourImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else
				return 0;
	  }
	  
	public Color getColorAtBlock(final int x, final int z, final int type) // Returns a Color (or an int for efficiency) of the biome color at a given block
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
		return true;
	}
	
	public void setJarLocation(File mjar)
	{
		minecraftJar = mjar;
	}
	
	private void autodetectMinecraft()
	{
		File minecraftFolderPath;
		// Figure out what platform this is.
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf( "mac" ) >= 0)
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),"Library");
			minecraftFolderPath = new File(minecraftFolderPath,"Application Support");
			minecraftFolderPath = new File(minecraftFolderPath,"minecraft");
		}
		else if (os.indexOf( "win" ) >= 0)
		{
			minecraftFolderPath = new File(System.getenv("APPDATA"),".minecraft");
		}
		else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0)
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
		}
	    else
		{
			minecraftFolderPath = new File(FileSystemView.getFileSystemView().getHomeDirectory(),".minecraft");
		}
		
		printm("Discovering minecraft.jar interface..."+NEW_LINE);
		minecraftJar = new File(new File(minecraftFolderPath,"bin"),"minecraft.jar");
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
		
		if (minecraftJar==null)
		{
			autodetectMinecraft();
		}
		
		ZipFile mcjar;
		if (!minecraftJar.exists())
		{
			printe("Failed to locate minecraft.jar"+NEW_LINE);
			printe("Path: " + minecraftJar.getAbsolutePath() +NEW_LINE);
			printe("Minecraft doesn't appear to be installed!"+NEW_LINE);

			return false;
		}
		else
		{
			class_listing = new ArrayList<String>();
			ZipEntry currentfile = null;
			try {
				
				// Setup the data buffer for copying stuff.
				int count;
				int BUFFER = 2048;
				byte data[] = new byte[BUFFER];
				
				mcjar = new ZipFile(minecraftJar);
				
				// Copy grasscolor.png and foliagecolor.png
				ZipEntry grasscolor = mcjar.getEntry("misc/grasscolor.png");
				ZipEntry foliagecolor = mcjar.getEntry("misc/foliagecolor.png");
				
				if (grasscolor != null)
				{
					grassColourImage = ImageIO.read(mcjar.getInputStream(grasscolor));
				}
				if (foliagecolor != null)
				{
					foliageColourImage = ImageIO.read(mcjar.getInputStream(foliagecolor));
				}
                
				ZipEntry mojang1 = mcjar.getEntry("META-INF/MOJANG_C.DSA");
				boolean needs_sig_removed = (mojang1 != null);
				
				Enumeration<? extends ZipEntry> e = mcjar.entries();
				
				FileOutputStream dest = new FileOutputStream(new File(minecraftJar.getAbsolutePath()+".new"));
				ZipOutputStream zout = null;
				if (needs_sig_removed)
					zout = new ZipOutputStream(new BufferedOutputStream(dest));
				String[] components;
				while(e.hasMoreElements())
				{
					currentfile = (ZipEntry) e.nextElement();
					components = currentfile.getName().split("/");
					
					if (components[components.length-1].toLowerCase().endsWith(".class") && currentfile.getName().length() > 5)
						class_listing.add(components[components.length-1].substring(0, components[components.length-1].length()-6));

					if (needs_sig_removed && !(currentfile.getName().endsWith(".DSA") || currentfile.getName().endsWith(".SF")))
					{
						BufferedInputStream is = new BufferedInputStream(mcjar.getInputStream(currentfile));
						zout.putNextEntry(currentfile);
						
			            while((count = is.read(data, 0, BUFFER)) != -1) {
			               zout.write(data, 0, count);
			            }
			            is.close();
					}
				}
				if (needs_sig_removed)
					zout.close();
				mcjar.close();
				
				if (needs_sig_removed)
				{
					minecraftJar.delete();
					(new File(minecraftJar.getAbsolutePath()+".new")).renameTo(minecraftJar);
					
					printm("Removed MOJANG signatures."+NEW_LINE);
				}
				
			} catch (ZipException e1) {
				System.out.println(currentfile.getName());
				e1.printStackTrace();
                printe("Failed to extract file from zip:" + currentfile.getName() +NEW_LINE);
				
			} catch (IOException e1) {
				e1.printStackTrace();
                printe("Failed to open new jar for writing!"+NEW_LINE);
				
			}
		}
		
		try {
			ClasspathUtil.addJarToClasspath(minecraftJar);
		} catch (IOException e) {
			printe("I can't seem to find your minecraft.jar file!"+NEW_LINE);
		}
		
		// The classes, methods, and fields we need, as strings:
		// Classes
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
		
		for (int i = 0; i<class_listing.size();i++)
		{
			class_signatures.add(ReflectionUtil.generateClassSignature(class_listing.get(i)));
			
			// Dump all the class signatures - helpful!
			//System.out.println(class_listing.get(i));
			//System.out.println(generateClassSignature(class_listing.get(i)));
			//System.out.println("\n");
		}
		
		int class_id = this.matchClassSignature(save_class_signature);
		if (class_id != -1)
		{
			save_class = class_listing.get(class_id);
			printm("Save class is: "+save_class+NEW_LINE);
		}
		else
		{
			printe(NEW_LINE + "Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for save class not found."+NEW_LINE);
			printe("Class listing ("+ Integer.toString(class_listing.size()) + "entires)" + NEW_LINE);
			
			for (int i=0; i<class_listing.size(); i++)
			{
				printe("\t"+ class_listing.get(i) + ".class" + NEW_LINE);
			}
			return false;
		}
		
		class_id = this.matchClassSignature(biome_gen_class_signature);
		if (class_id != -1)
		{
			biome_gen_class = class_listing.get(class_id);
			printm("Biome Gen class is: "+biome_gen_class+NEW_LINE);
		}
		else
		{
			printe("Deobfuscation of minecraft.jar failed."+NEW_LINE);
			printe("Signature match for biome gen class not found."+NEW_LINE);
			return false;
		}
	
		// Get the classes we need using reflection
		
		try {
			minecraftSaveClass = Class.forName(save_class);
			biomeGeneratorClass = Class.forName(biome_gen_class);
		} catch (ClassNotFoundException e2) {
			printe("Can't find Minecraft! Looked here:"+NEW_LINE);
			printe(minecraftJar.getAbsolutePath()+NEW_LINE);
			printe("Make certain minecraft.jar is in that location."+NEW_LINE);
			return false;
		}
		
		// Setup the MINECRAFTSAVECLASS constructor
		// Client
		Class<?> partypes[] = new Class[2];
        partypes[0] = File.class;
        partypes[1] = String.class;
        
		try {
			createMinecraftSave = minecraftSaveClass.getConstructor(partypes);
		} catch (SecurityException e1) {
			printe("Could not bind MinecraftSave Constructor (security issue)"+NEW_LINE);
			printe("I'm trying to delete the Mojang signatures for you."+NEW_LINE);
			printe("Restart the program and try again."+NEW_LINE);
			return false;
		} catch (NoSuchMethodException e1) {
			
			// This could mean that you are operating on a server JAR. Try this:
			try {
				printm("Looks like this might be a server JAR. Keep trying..."+NEW_LINE);
				partypes = new Class[4];
		        partypes[0] = File.class;
		        partypes[1] = String.class;
		        partypes[2] = Long.TYPE;
				partypes[3] = Class.forName(class_listing.get(this.matchClassSignature(SERVER_RAND_CLASS_REF)));
				createMinecraftSave = minecraftSaveClass.getConstructor(partypes);
				isServerJar = true;
			} catch (Throwable e) {
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
	
	// END CLASS DEF
}
