package org.tectonicus.biomeextractor;

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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Reconstructs biome colormap coordinates for historical Minecraft worlds by running the generator of a matching
 * Minecraft JAR with the world's seed.
 *
 * <p>Typical use:
 * <pre>{@code
 * try (WorldProcessor processor = new WorldProcessor()) {
 *     processor.setJarLocation(minecraftJar);
 *     processor.setWorldFolder(worldDirectory);
 *     if (!processor.bindToMinecraft() || !processor.loadWorld()) {
 *         throw new IllegalStateException("Unsupported Minecraft JAR or world");
 *     }
 *     byte[] coordinates = processor.getCoordsAtBlock(0, 0);
 * }
 * }</pre>
 *
 * <p>Each processor owns an isolated class loader and a temporary copy of the Minecraft JAR with its signatures
 * removed; the source JAR is never modified. {@link #close()} releases both, so close the processor once extraction
 * has stopped. Instances are not thread-safe, apart from {@link #stopRunning()}.
 *
 * <p>Bulk queries cover a {@code width} by {@code depth} area starting at block ({@code x}, {@code z}) and return
 * results in row-major order: the value for block ({@code x + i}, {@code z + j}) is at index {@code i + j * width}.
 *
 * <p>Coordinate and climate queries need no images. Colour queries need a real 256x256 colormap, either loaded from
 * the client JAR's {@code misc/} folder or supplied with {@link #setBiomeImages(File, File, File)}; missing colormaps
 * are never approximated.
 */
public class WorldProcessor implements Runnable, AutoCloseable
{
	/** The colormaps a block colour can be looked up in. */
	public enum ColourType
	{
		/** {@code grasscolor.png}. */
		GrassColour,
		/** {@code foliagecolor.png}. */
		FoliageColour,
		/** {@code watercolor.png}, which only some Minecraft versions include. */
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

	private final Map<ColourType, BufferedImage> jarColourMaps = new EnumMap<>(ColourType.class);
	private final Map<ColourType, BufferedImage> suppliedColourMaps = new EnumMap<>(ColourType.class);
	private boolean coordinatesOnly;
	
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
	
	/** Creates a processor that prints only errors, to standard output. */
	public WorldProcessor()
	{
		this.useGUI = false;
		this.errorsOnly = true;
	}

	WorldProcessor(MinecraftBiomeExtractorGUI gui, final boolean errorsOnly, final boolean flush)
	{
		if (gui == null)
			useGUI = false;
		else
			this.gui = gui;
		
		this.flush = flush;
		this.errorsOnly = errorsOnly;
	}
	
	/**
	 * Returns whether a Minecraft JAR is currently bound.
	 *
	 * @return true after a successful {@link #bindToMinecraft()} and until {@link #close()}
	 */
	public boolean isBound()
	{
		return bound;
	}

	/**
	 * Asks a running {@link #run()} to stop after the current region. Regions already written are kept, so a later
	 * run continues where this one stopped. Safe to call from another thread.
	 */
	public void stopRunning()
	{
		die = true;
		return;
	}

	/**
	 * Sets the world save folder to process.
	 *
	 * @param world_folder the save folder containing {@code level.dat}
	 * @return false, clearing the current world, if the folder does not exist
	 */
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
	
	/**
	 * Sets where {@link #run()} writes its output.
	 *
	 * @param dir the output folder, or null for a {@code biomes} folder inside the world folder
	 */
	public void setOutputDir(File dir)
	{
		this.outputDir = dir;
	}

	/**
	 * Sets whether {@link #run()} deletes existing output first. Otherwise regions that already have a
	 * {@code .biome} file are skipped.
	 *
	 * @param flush true to regenerate all output
	 */
	public void setFlush(boolean flush)
	{
		this.flush = flush;
	}

	/**
	 * Sets whether {@link #run()} exports only {@code .biome} coordinates, without requiring or writing colormap
	 * images.
	 *
	 * @param coordinatesOnly true for an image-free export
	 */
	public void setCoordinatesOnly(boolean coordinatesOnly) {
		this.coordinatesOnly = coordinatesOnly;
	}

	/**
	 * Exports the whole world: one 524288-byte {@code b.<x>.<z>.biome} file per occupied 32-by-32-chunk region, plus
	 * the available colormap PNGs unless {@linkplain #setCoordinatesOnly(boolean) coordinates only} is set. Each file
	 * holds two bytes per block, colormap y then x, for 512 by 512 blocks in row-major order.
	 *
	 * <p>Requires a bound JAR and a world folder; the world is loaded automatically, before any existing output is
	 * {@linkplain #setFlush(boolean) flushed}. Progress and errors are reported as text rather than thrown, except
	 * for a missing grass or foliage colormap, which throws {@link IllegalStateException} before anything is
	 * deleted.
	 */
	@Override
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
		
		// Validate before flush can delete a previously successful export.
		if (!coordinatesOnly) {
			requireColourMap(ColourType.GrassColour);
			requireColourMap(ColourType.FoliageColour);
		}

		if (outputDir == null)
		{
			outputDir = new File(worldFolder,"biomes");
			printm("No output dir specified." + NEW_LINE + "Placing output in "+ outputDir.getAbsolutePath() + NEW_LINE);
		}
		
		printm("Opening " + worldFolder.getName() + "..." + NEW_LINE);

		// Load the world before flushing, so a JAR that cannot read it leaves the previous export intact.
		printm("Locating Minecraft save..."+NEW_LINE);
		if (!this.loadWorld())
		{
			printe("Minecraft classes failed to load the world."+ NEW_LINE);
			return;
		}

		if (outputDir.exists() && flush)
		{
			printm("Deleting existing biome data in " + outputDir.getAbsolutePath() + NEW_LINE);
			FileUtils.deleteDirectory(outputDir);
		}

		this.setupDataFolder(outputDir);

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
	
	/**
	 * Returns the name of the binding profile that matched the bound JAR. Profiles name a range of Minecraft API
	 * generations, not an exact version.
	 *
	 * @return the profile name, or null when no JAR is bound
	 */
	public String getBindingProfileName() {
		return bindings == null ? null : bindings.profile.displayName;
	}

	/**
	 * Loads the world folder with the bound JAR so the generator uses the world's seed. Call it after
	 * {@link #bindToMinecraft()} and before any coordinate, climate or colour query.
	 *
	 * @return false, after reporting why, if no JAR is bound, no world is set, or Minecraft cannot load the world
	 */
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

	private void setupDataFolder(File directory) {
		try {
			Files.createDirectories(directory.toPath());
			if (!coordinatesOnly) {
				for (ColourType type : ColourType.values()) {
					File output = new File(directory, colourMapName(type));
					BufferedImage image = colourMap(type);
					if (image != null) {
						if (!ImageIO.write(image, "png", output))
							throw new IOException("No PNG writer available for " + output);
					} else {
						Files.deleteIfExists(output.toPath());
						printm("No real " + colourMapName(type) + " is available; this image will not be exported." + NEW_LINE);
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Could not write biome output to " + directory, e);
		}
	}
    
	/**
	 * Returns the generator's temperature for an area of blocks.
	 *
	 * @param dest an array to reuse, or null; always use the returned array, which may be a new one
	 * @param x the first block's x coordinate
	 * @param z the first block's z coordinate
	 * @param dx the area's width, in blocks
	 * @param dz the area's depth, in blocks
	 * @return temperatures from 0 to 1, in row-major order
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public float[] getTemperaturesAtBlocks(float[] dest, int x, int z, int dx, int dz) throws Exception {
		return requireBindings().climate(true, dest, x, z, dx, dz);
	}

	/**
	 * Returns the generator's temperature at one block.
	 *
	 * @param x the block's x coordinate
	 * @param z the block's z coordinate
	 * @return a temperature from 0 to 1
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public double getTemperatureAtBlock(int x, int z) throws Exception {
		return requireBindings().climateAtBlock(true, x, z);
	}

	/**
	 * Returns the generator's moisture (rainfall) for an area of blocks.
	 *
	 * @param dest an array to reuse, or null; always use the returned array, which may be a new one
	 * @param x the first block's x coordinate
	 * @param z the first block's z coordinate
	 * @param dx the area's width, in blocks
	 * @param dz the area's depth, in blocks
	 * @return moistures from 0 to 1, in row-major order
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public float[] getMoisturesAtBlocks(float[] dest, int x, int z, int dx, int dz) throws Exception {
		return requireBindings().climate(false, dest, x, z, dx, dz);
	}

	/**
	 * Returns the generator's moisture (rainfall) at one block.
	 *
	 * @param x the block's x coordinate
	 * @param z the block's z coordinate
	 * @return a moisture from 0 to 1
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public double getMoistureAtBlock(int x, int z) throws Exception {
		return requireBindings().climateAtBlock(false, x, z);
	}

	/**
	 * Returns colormap coordinates for an area of blocks. For temperature {@code t} and moisture {@code m}, a block's
	 * colormap pixel is x = {@code (1 - t) * 255}, y = {@code (1 - m * t) * 255}.
	 *
	 * @param dest an array to reuse, or null; reused only if its length is exactly {@code dx * dz}
	 * @param temps a scratch array for temperatures, or null
	 * @param moists a scratch array for moistures, or null
	 * @param x the first block's x coordinate
	 * @param z the first block's z coordinate
	 * @param dx the area's width, in blocks
	 * @param dz the area's depth, in blocks
	 * @return coordinates in row-major order
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws IllegalArgumentException if {@code dx} or {@code dz} is not positive
	 * @throws Exception if the Minecraft generator fails
	 */
	public ByteCoord[] getCoordsAtBlocks(ByteCoord[] dest, float[] temps, float[] moists, int x, int z, int dx, int dz)
			throws Exception {
		return requireBindings().coordinates(dest, temps, moists, x, z, dx, dz);
	}

	/**
	 * Returns which blocks in an area are in the generated swamp biome, for consumers applying the Beta 1.8+ swamp
	 * tint. Profiles older than Beta 1.8 have no swamp tint and return an all-false mask.
	 *
	 * @param x the first block's x coordinate
	 * @param z the first block's z coordinate
	 * @param width the area's width, in blocks
	 * @param depth the area's depth, in blocks
	 * @return true for swamp blocks, in row-major order
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public boolean[] getSwampMaskAtBlocks(int x, int z, int width, int depth) throws Exception {
		return requireBindings().swampMask(x, z, width, depth);
	}

	private MinecraftBindings requireBindings() {
		if (bindings == null) {
			throw new IllegalStateException("Bind a Minecraft JAR before requesting biome data");
		}
		return bindings;
	}

	/**
	 * Returns the colormap coordinates for one block.
	 *
	 * @param x the block's x coordinate
	 * @param z the block's z coordinate
	 * @return two bytes, the pixel's x then y in a 256x256 colormap; read them as unsigned ({@code & 0xff})
	 * @throws IllegalStateException if no JAR is bound or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 * @see #getCoordsAtBlocks(ByteCoord[], float[], float[], int, int, int, int)
	 */
	public byte[] getCoordsAtBlock(final int x, final int z) throws Exception
	{
		ByteCoord[] coords = getCoordsAtBlocks(null, null, null, x, z, 1, 1);
		byte[] coords_translated = new byte[2];
		
		coords_translated[0] = coords[0].x;
		coords_translated[1] = coords[0].y;
		return coords_translated;
	}
	  
	/**
	 * Returns a block's colour from a real colormap. This does not apply the swamp tint; see
	 * {@link #getSwampMaskAtBlocks(int, int, int, int)}.
	 *
	 * @param x the block's x coordinate
	 * @param z the block's z coordinate
	 * @param type the colormap to read
	 * @return the colormap pixel as packed ARGB, as {@link BufferedImage#getRGB(int, int)} returns it
	 * @throws IllegalStateException if the colormap is missing, no JAR is bound, or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 */
	public int getRGBAtBlock(int x, int z, ColourType type) throws Exception {
		BufferedImage image = requireColourMap(type);
		byte[] coords = getCoordsAtBlock(x, z);
		return image.getRGB(coords[0] & 0xff, coords[1] & 0xff);
	}

	/**
	 * Returns a block's colour from a real colormap, as a {@link Color}.
	 *
	 * @param x the block's x coordinate
	 * @param z the block's z coordinate
	 * @param type the colormap to read
	 * @return the colormap pixel
	 * @throws IllegalStateException if the colormap is missing, no JAR is bound, or no world is loaded
	 * @throws Exception if the Minecraft generator fails
	 * @see #getRGBAtBlock(int, int, ColourType)
	 */
	public Color getColorAtBlock(final int x, final int z, final ColourType type) throws Exception
	{
		return new Color(getRGBAtBlock(x, z, type));
	}

	/**
	 * Supplies a grass colormap and clears any foliage and water overrides.
	 *
	 * @param grasscolor a 256x256 image, or null
	 * @return false if the image cannot be read
	 * @see #setBiomeImages(File, File, File)
	 */
	public boolean setBiomeImages(final File grasscolor)
	{
		return setBiomeImages(grasscolor, null, null);
	}

	/**
	 * Supplies grass and foliage colormaps and clears any water override.
	 *
	 * @param grasscolor a 256x256 image, or null
	 * @param foliagecolor a 256x256 image, or null
	 * @return false if an image cannot be read
	 * @see #setBiomeImages(File, File, File)
	 */
	public boolean setBiomeImages(final File grasscolor, final File foliagecolor)
	{
		return setBiomeImages(grasscolor, foliagecolor, null);
	}

	/**
	 * Supplies colormaps that take precedence over those in the Minecraft JAR. A successful call replaces all earlier
	 * overrides, and overrides persist when another JAR is bound. If any image is invalid, nothing changes.
	 *
	 * @param grasscolor a 256x256 image, or null for no override
	 * @param foliagecolor a 256x256 image, or null for no override
	 * @param watercolor a 256x256 image, or null for no override
	 * @return false, after reporting why, if an image cannot be read or is not 256x256
	 */
	public boolean setBiomeImages(final File grasscolor, final File foliagecolor, final File watercolor)
	{
		Map<ColourType, BufferedImage> images = new EnumMap<>(ColourType.class);
		File[] files = {grasscolor, foliagecolor, watercolor};
		try {
			for (ColourType type : ColourType.values()) {
				File file = files[type.ordinal()];
				if (file != null) {
					try (InputStream input = Files.newInputStream(file.toPath())) {
						images.put(type, readColourMap(input, file.toString()));
					}
				}
			}
		} catch (IOException e) {
			printe("Could not load biome colormaps: " + e.getMessage() + NEW_LINE);
			return false;
		}
		suppliedColourMaps.clear();
		suppliedColourMaps.putAll(images);
		return true;
	}

	private BufferedImage colourMap(ColourType type) {
		if (type == null)
			throw new IllegalArgumentException("Specify a biome color type");
		BufferedImage supplied = suppliedColourMaps.get(type);
		return supplied != null ? supplied : jarColourMaps.get(type);
	}

	private BufferedImage requireColourMap(ColourType type) {
		BufferedImage image = colourMap(type);
		if (image == null)
			throw new IllegalStateException("Missing real colormap " + colourMapName(type)
					+ ". Use a client JAR containing it or supply a 256x256 image with setBiomeImages."
					+ " Coordinate extraction remains available; use -coordsOnly for coordinate-only export.");
		return image;
	}

	private static String colourMapName(ColourType type) {
		switch (type) {
			case GrassColour: return "grasscolor.png";
			case FoliageColour: return "foliagecolor.png";
			case WaterColour: return "watercolor.png";
			default: throw new IllegalArgumentException("Unsupported biome color type: " + type);
		}
	}

	private static BufferedImage readColourMap(InputStream input, String source) throws IOException {
		BufferedImage image = ImageIO.read(input);
		if (image == null || image.getWidth() != 256 || image.getHeight() != 256)
			throw new IOException("Expected a readable 256x256 colormap: " + source);
		return image;
	}

	private void loadColourMaps(ZipFile jar) {
		for (ColourType type : ColourType.values()) {
			ZipEntry entry = jar.getEntry("misc/" + colourMapName(type));
			if (entry != null) {
				try (InputStream input = jar.getInputStream(entry)) {
					jarColourMaps.put(type, readColourMap(input, entry.getName()));
				} catch (IOException e) {
					printe("Could not load " + colourMapName(type) + ": " + e.getMessage() + NEW_LINE);
				}
			}
		}
	}
	
	/**
	 * Sets the Minecraft client or server JAR to bind to. Use the JAR matching the generator that created the world.
	 *
	 * @param mjar the JAR, or null to look for {@code minecraft.jar} in the local Minecraft installation
	 */
	public void setJarLocation(File mjar)
	{
		minecraftJar = mjar;
	}

	/**
	 * Binds to the JAR set with {@link #setJarLocation(File)}, releasing any previous binding first. The JAR is copied
	 * without its signatures, its colormaps are loaded, and each known binding profile is tried until one matches.
	 *
	 * @return false, after reporting why, if the JAR is missing, unreadable, or matches no profile
	 */
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

	/**
	 * Releases this processor's class loader and temporary JAR. Do not call while extracting. The processor can be
	 * bound again afterwards; supplied colormaps are kept.
	 *
	 * @throws IOException if the temporary JAR cannot be deleted
	 */
	@Override
	public void close() throws IOException {
		bound = false;
		bindings = null;
		jarColourMaps.clear();
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
				loadColourMaps(mcjar);
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
