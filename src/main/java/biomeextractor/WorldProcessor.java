package biomeextractor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class WorldProcessor implements Runnable, AutoCloseable
{
	public enum ColourType
	{
		GrassColour,
		FoliageColour,
		WaterColour
	}
	
	private static final String NEW_LINE = "\n";
	
	private MinecraftBiomeExtractorGUI gui = null;
	
	// Minecraft Class Bindings
	private File minecraftJar = null;
	private File minecraftJarCopy;
	private URLClassLoader minecraftClassLoader;
	private MinecraftBindings bindings;
	private boolean useGUI = true;

	// Storage for the grasscolor and foliagecolor pngs
	private BufferedImage grassColorImage = null;
	private BufferedImage foliageColorImage = null;
	private BufferedImage waterColorImage = null;
	private boolean useDefaultBiomeImages = true;
	
	// World Variables
	private File worldFolder = null;
	private List<int[]> chunks;
	
	// Output
	private File outputDir = null;
	
	// Internal class state variables
	private boolean flush = false;
	private boolean errorsOnly = false;
	private int lastPercent = 0;
	private int countPercents = 0;
	
	private boolean bound = false;	// Is set to true if the extractor bound to Minecraft properly
	private volatile boolean die = false;  	// Set to true to abort the process if run in a separate thread.
	
	public WorldProcessor()
	{
		this.useGUI = false;
		this.errorsOnly = true;
		
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
		
		setupDefaultBiomeImages();
		
	}
	
	private void setupDefaultBiomeImages()
	{
		grassColorImage = DefaultBiomeColors.create(ColourType.GrassColour);
		foliageColorImage = DefaultBiomeColors.create(ColourType.FoliageColour);
		waterColorImage = DefaultBiomeColors.create(ColourType.WaterColour);
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
			
			try {
				chunks = WorldRegionScanner.scan(worldFolder);
			} catch (IOException e) {
				printe("Could not scan world: " + e.getMessage() + NEW_LINE);
				return;
			}
			if (chunks.isEmpty()) {
				printm("No saved Overworld chunks found. No biome regions were generated." + NEW_LINE);
				return;
			}
			printm("World Size: "+ Integer.toString(chunks.size()) + " regions" + NEW_LINE);
			printm("Saving biome data...  (press esc to cancel)" + NEW_LINE);
			
			ByteCoord[] coords = new ByteCoord[512 * 512];
			float[] temps = new float[512 * 512];
			float[] moists = new float[512 * 512];
			
			for (int x = 0; x<chunks.size();x++)
			{		
					final int fromX = chunks.get(x)[0]*512;
					final int fromZ = chunks.get(x)[1]*512;
					
					File biomeFile = new File(outputDir, "b." + Integer.toString(chunks.get(x)[0])+"."+Integer.toString(chunks.get(x)[1])+".biome");
					
					if (!biomeFile.exists())
					{
						try
						{
							getCoordsAtBlocks(coords, temps, moists, fromX, fromZ, 512, 512);
						}
						catch (Throwable e)
						{
							printe("Minecraft Biome Extractor has failed you! My apologies."+NEW_LINE);
							printe("Halting biome extraction. Check online for an update."+NEW_LINE);
							return;
						}
						
						for(int i = 0; i < 512; i++)
						{
							for(int j = 0; j < 512; j++)
							{	
								data[2*i + j*1024] = coords[i + 512*j].y;
								data[2*i + 1 + j*1024] = coords[i + 512*j].x;
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
						printm(percent +"% ... ");
						
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
	
	/** Returns the selected API profile, or null when no JAR is bound. */
	public String getBindingProfileName() {
		return bindings == null ? null : bindings.profile.displayName;
	}

	public boolean loadWorld() {
		if (bindings == null || worldFolder == null) {
			printe("Bind a Minecraft JAR and select a world before loading." + NEW_LINE);
			return false;
		}
		try {
			bindings.loadWorld(worldFolder);
			printm("Level Loaded!" + NEW_LINE);
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) {
			printe("Could not load world using " + bindings.profile.displayName + ": " + e + NEW_LINE);
			return false;
		}
	}

    // If no EXTRACTEDBIOMES folder exists, make one.
    // Copy in grasscolor.png, foliagecolor.png, and watercolor.png
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
			if (grassColorImage != null)
			{
				ImageIO.write(grassColorImage, "png", new File(biomesFolder,"grasscolor.png"));
			}
			if (foliageColorImage != null)
			{
				ImageIO.write(foliageColorImage, "png", new File(biomesFolder,"foliagecolor.png"));
			}
			if (waterColorImage != null)
			{
				ImageIO.write(waterColorImage, "png", new File(biomesFolder,"watercolor.png"));
			}
		}
		catch (IOException e)
		{
			printm("Failed to write biome color pngs. Continuing..."+NEW_LINE);
		}
    }
    
	public float[] getTemperaturesAtBlocks(float[] dest, int x, int z, int dx, int dz) throws Exception {
		return requireBindings().climate(true, dest, x, z, dx, dz);
	}

	public double getTemperatureAtBlock(int x, int z) throws Exception {
		return requireBindings().climateAtBlock(true, x, z);
	}

	public float[] getMoisturesAtBlocks(float[] dest, int x, int z, int dx, int dz) throws Exception {
		return requireBindings().climate(false, dest, x, z, dx, dz);
	}

	public double getMoistureAtBlock(int x, int z) throws Exception {
		return requireBindings().climateAtBlock(false, x, z);
	}

	public ByteCoord[] getCoordsAtBlocks(ByteCoord[] dest, float[] temps, float[] moists, int x, int z, int dx, int dz)
			throws Exception {
		return requireBindings().coordinates(dest, temps, moists, x, z, dx, dz);
	}

	private MinecraftBindings requireBindings() {
		if (bindings == null) {
			throw new IllegalStateException("Bind a Minecraft JAR before requesting biome data");
		}
		return bindings;
	}

	public byte[] getCoordsAtBlock(final int x, final int z) throws Exception // Returns the location of the biome color in the 256x256 biome PNG (two bytes)
	{
		ByteCoord[] coords = getCoordsAtBlocks(null, null, null, x, z, 1, 1);
		byte[] coords_translated = new byte[2];
		
		coords_translated[0] = coords[0].x;
		coords_translated[1] = coords[0].y;
		return coords_translated;
	}
	  
	  // Returns the biome color at a given block, packed in an int as RGB
	  public int getRGBAtBlock(final int x, final int z, final ColourType type) throws Exception
	  {
		  final byte[] coords;
		  try
		  {
			  coords = getCoordsAtBlock(x,z);
		  }
		  catch (Throwable e1)
		  {
			  throw(new Exception("Biome Extractor has failed to properly interface with Minecraft."));
		  }
		  	
			if (type == ColourType.GrassColour)
				return grassColorImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else if (type == ColourType.FoliageColour)
				return foliageColorImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else if (type == ColourType.WaterColour)
				return waterColorImage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else
				return 0;
	  }
	  
	// Returns the biome color at a given block
	public Color getColorAtBlock(final int x, final int z, final ColourType type) throws Exception
	{
		try
		{
			return new Color(getRGBAtBlock(x, z, type));
		}
		catch (Throwable e1)
		{
			throw(new Exception("Biome Extractor has failed to properly interface with Minecraft. (getColorAtBlock)"));
		}
	}
	
	public boolean setBiomeImages(final File grasscolor)
	{
		return setBiomeImages(grasscolor, null, null);
	}
	
	public boolean setBiomeImages(final File grasscolor, final File foliagecolor)
	{
		return setBiomeImages(grasscolor, foliagecolor, null);
	}
	  
	public boolean setBiomeImages(final File grasscolor, final File foliagecolor, final File watercolor)
	{
			try 
			{
				if (grasscolor != null)
				{
					grassColorImage = ImageIO.read(grasscolor);
				}
				if (foliagecolor != null)
				{
					foliageColorImage = ImageIO.read(foliagecolor);
				}
				if (watercolor != null)
				{
					waterColorImage = ImageIO.read(foliagecolor);
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
		try {
			close();
		} catch (IOException e) {
			printe("Could not release the previous Minecraft JAR: " + e.getMessage() + NEW_LINE);
			return false;
		}
		try {
			return bindMinecraft();
		} finally {
			if (!bound) {
				try {
					close();
				} catch (IOException e) {
					printe("Could not release the Minecraft JAR: " + e.getMessage() + NEW_LINE);
				}
			}
		}
	}

	/** Releases this processor's class loader and temporary JAR. Do not call while extracting. */
	@Override
	public void close() throws IOException {
		bound = false;
		bindings = null;
		URLClassLoader loader = minecraftClassLoader;
		minecraftClassLoader = null;
		try {
			if (loader != null) {
				loader.close();
			}
		} finally {
			if (minecraftJarCopy != null) {
				Files.deleteIfExists(minecraftJarCopy.toPath());
				minecraftJarCopy = null;
			}
		}
	}

	private boolean bindMinecraft()
	{
		// This method kind of crazy. 
		// 1.) Find the minecraft.jar
		// 2.) Check for MOJANG signatures
		//		- If they exist rewrite the zip to not include them
		// 3.) Take a note of all files ending in .class
		// 4.) Copy grasscolor, foliagecolor and watercolor to memory.
		// 5.) Close the zip file
		// 6.) Open an independent class loader for the Minecraft JAR
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
		if (minecraftJar == null || !minecraftJar.isFile())
		{
			printe("Failed to locate minecraft.jar"+NEW_LINE);
			printe("Path: " + minecraftJar + NEW_LINE);
			printe("Minecraft doesn't appear to be installed!"+NEW_LINE);

			return false;
		}
		
		ArrayList<String> classListing = new ArrayList<String>();
		try {
			// Use an owned temporary copy so input JARs remain untouched and processors do not share files.
			minecraftJarCopy = Files.createTempFile("biomeextractor-", ".jar").toFile();
			stripMinecraftJar(minecraftJar, minecraftJarCopy);
			try (ZipFile mcjar = new ZipFile(minecraftJarCopy)) {
				if (useDefaultBiomeImages) {
					setupDefaultBiomeImages();
					ZipEntry grasscolor = mcjar.getEntry("misc/grasscolor.png");
					ZipEntry foliagecolor = mcjar.getEntry("misc/foliagecolor.png");
					ZipEntry watercolor = mcjar.getEntry("misc/watercolor.png");
					if (grasscolor != null) {
						try (InputStream input = mcjar.getInputStream(grasscolor)) {
							grassColorImage = ImageIO.read(input);
						}
					}
					if (foliagecolor != null) {
						try (InputStream input = mcjar.getInputStream(foliagecolor)) {
							foliageColorImage = ImageIO.read(input);
						}
					}
					if (watercolor != null) {
						try (InputStream input = mcjar.getInputStream(watercolor)) {
							waterColorImage = ImageIO.read(input);
						}
					}
				}
				Enumeration<? extends ZipEntry> entries = mcjar.entries();
				while (entries.hasMoreElements()) {
					String name = entries.nextElement().getName();
					if (name.endsWith(".class") && name.length() > 6) {
						classListing.add(name.substring(0, name.length() - 6).replace('/', '.'));
					}
				}
			}
			minecraftClassLoader = ClasspathUtil.createClassLoader(minecraftJarCopy);
		} catch (IOException e) {
			printe("Could not open Minecraft JAR: " + e.getMessage() + NEW_LINE);
			return false;
		}

		Map<String, String> signatures = new LinkedHashMap<>();
		for (String className : classListing) {
			signatures.put(className, ReflectionUtil.generateClassSignature(className, minecraftClassLoader));
		}
		for (BindingProfile profile : BindingProfile.values()) {
			try {
				MinecraftBindings candidate = new MinecraftBindings(profile, minecraftClassLoader, signatures);
				bindings = candidate;
				bound = true;
				printm("Ready! Using " + profile.displayName + (candidate.server ? " server" : " client") + " bindings." + NEW_LINE);
				return true;
			} catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
				printm(profile.displayName + " bindings did not match: " + e.getMessage() + NEW_LINE);
			}
		}
		printe("No compatible Minecraft binding profile matched this JAR." + NEW_LINE);
		return false;
	}

	private static void stripMinecraftJar(File srcJar, File destJar) throws IOException {
		try (ZipFile source = new ZipFile(srcJar);
				ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destJar)))) {
			byte[] buffer = new byte[8192];
			Enumeration<? extends ZipEntry> entries = source.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName().toUpperCase(java.util.Locale.ROOT);
				if (name.startsWith("META-INF/") && (name.endsWith(".DSA") || name.endsWith(".SF")
						|| name.endsWith(".RSA") || name.endsWith(".EC"))) {
					continue;
				}
				output.putNextEntry(new ZipEntry(entry.getName()));
				try (InputStream input = source.getInputStream(entry)) {
					int count;
					while ((count = input.read(buffer)) != -1) {
						output.write(buffer, 0, count);
					}
				}
				output.closeEntry();
		}
	}
}
}
