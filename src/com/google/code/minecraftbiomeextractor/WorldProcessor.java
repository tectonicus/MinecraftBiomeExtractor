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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.filechooser.FileSystemView;

public class WorldProcessor implements Runnable {
	
	static private final String newline = "\n";
	
	static private final String saveclass_ref = "carg:class java.io.File carg:class java.lang.String +carg:class java.io.File carg:class java.lang.String carg:long +carg:class java.lang.String carg:class ox carg:long +carg:class cx carg:class ox +carg:class java.io.File carg:class java.lang.String carg:long carg:class ox +ret:void+arg:int arg:int arg:int ret:void+ret:void+arg:float ret:class+arg:int arg:int ret:class+arg:int arg:int arg:int arg:int ret:boolean+arg:class ret:boolean+arg:class ret:void+ret:void+arg:int arg:int arg:int ret:float+arg:int arg:int arg:int arg:class ret:void+arg:class arg:class ret:interface java.util.List+arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:class ret:boolean+arg:class arg:int arg:int arg:int arg:int ret:void+arg:class ret:boolean+arg:class arg:class ret:void+arg:int arg:int arg:int ret:class+arg:int arg:int ret:class+arg:class ret:void+arg:interface ju ret:void+ret:void+arg:float ret:float+arg:interface java.util.List ret:void+arg:int arg:int arg:int arg:int ret:void+arg:class ret:int+arg:int arg:int arg:int arg:int arg:int ret:boolean+ret:boolean+arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:boolean+arg:class arg:int arg:int arg:int arg:float ret:class+arg:class ret:void+ret:class+arg:class ret:interface bd+arg:int arg:int ret:int+arg:class ret:void+arg:boolean arg:interface ps ret:void+arg:int ret:boolean+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int arg:boolean ret:int+arg:class arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int ret:int+arg:class arg:class ret:class+arg:class arg:class arg:boolean ret:class+arg:class arg:class arg:float arg:float ret:void+arg:double arg:double arg:double arg:class arg:float arg:float ret:void+arg:class arg:int arg:int arg:int ret:void+arg:class arg:double arg:double arg:double arg:double arg:double arg:double ret:void+arg:class ret:boolean+arg:interface ju ret:void+arg:class arg:class ret:interface java.util.List+arg:float ret:int+arg:class arg:float ret:class+arg:class arg:boolean ret:void+arg:class ret:boolean+arg:class arg:class arg:class ret:boolean+arg:class arg:class ret:boolean+arg:class arg:double arg:double arg:double arg:float ret:void+arg:class arg:class ret:float+arg:class ret:class+arg:int arg:int arg:int arg:class ret:void+arg:interface ps ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int ret:void+arg:class arg:int arg:int arg:int arg:int arg:int arg:int arg:boolean ret:void+arg:boolean ret:boolean+arg:class arg:class ret:interface java.util.List+arg:interface java.util.List ret:void+arg:int arg:int arg:int arg:int arg:boolean ret:boolean+arg:class arg:class arg:float ret:class+arg:class arg:class ret:class+arg:class arg:double ret:class+arg:double arg:double arg:double arg:double ret:class+arg:int arg:int arg:int arg:int arg:int arg:int arg:class ret:void+arg:long ret:void+arg:class arg:int arg:int arg:int ret:boolean+arg:class arg:byte ret:void+arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int ret:void+arg:int arg:int arg:int arg:int ret:void+ret:void+arg:int arg:int arg:int ret:boolean+ret:void+arg:int arg:int arg:int arg:int ret:void+arg:class ret:void+ret:class+arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:class+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:boolean+ret:void+ret:interface java.util.List+arg:int arg:int arg:int arg:int ret:boolean+arg:int arg:int arg:int ret:boolean+ret:void+arg:class ret:void+arg:int arg:int arg:int ret:int+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int ret:int+arg:float ret:float+ret:void+arg:int arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:void+arg:int arg:int arg:int ret:boolean+arg:class ret:void+arg:int arg:int ret:int+ret:boolean+arg:int arg:int arg:int arg:int ret:boolean+arg:float ret:class+arg:int arg:int arg:int ret:boolean+ret:void+fld:boolean+fld:interface java.util.List+fld:interface java.util.List+fld:interface java.util.List+fld:class+fld:interface java.util.Set+fld:interface java.util.List+fld:interface java.util.List+fld:long+fld:long+fld:int+fld:int+fld:int+fld:boolean+fld:long+fld:int+fld:int+fld:class+fld:int+fld:int+fld:int+fld:boolean+fld:class+fld:interface java.util.List+fld:interface bd+fld:class+fld:class+fld:long+fld:class+fld:long+fld:class+fld:boolean+fld:class+fld:int+fld:int+fld:interface java.util.Set+fld:int+fld:interface java.util.List+fld:boolean+";
	static private final String biomegenclass_ref = "+carg:class cx +arg:int arg:int ret:double+arg:int arg:int ret:class+arg:class ret:class+arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+arg:class arg:int arg:int arg:int arg:int ret:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+fld:class+";
	static private final String serverrandclass_ref = "+ret:interface cg+ret:void+ret:void+arg:class ret:void+arg:int arg:int ret:boolean+arg:long arg:float ret:float+arg:int ret:class+arg:class ret:interface bd+fld:class+fld:class+fld:boolean+fld:boolean+fld:boolean+fld:class+fld:int+fld:class+";
	
	private MinecraftBiomeExtractorGUI gui = null;
	
	// Minecraft Class Bindings
	private File minecraftJar = null;
	private ArrayList<String> class_listing;
	private ArrayList<String> class_signatures;
	private boolean useGUI = true;
	private Class<?> MINECRAFTSAVECLASS;
	private Class<?> BIOMEGENERATORCLASS;
	private Constructor<?> createMinecraftSave;
	private Constructor<?> createBiomeGenerator;
	private Method generateForLoaction;
    private Field loadedField = null;
    private Field genTemp;
    private Field genMoist;
    private Object MinecraftSave;
    private Object BiomeGenerator;
    private Object arglist[]; // Used to call the biome generator.
    
    // Storage for the grasscolor and foliagecolor pngs
	private BufferedImage grasscolorimage = null;
	private BufferedImage foliagecolorimage = null;
	
	// World Variables
	private File world_folder = null;
	private int chunk_min_x = 0;
	private int chunk_max_x = 0;
	private int chunk_min_z = 0;
	private int chunk_max_z = 0;
	
	// Output
	private File outputDir = null;
	
	// Internal class state variables
	private boolean flush = false;
    boolean isServerJar = false;
	private boolean errorsOnly = false;
	private int last_percent = 0;
	private int count_percents = 0;
	
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
		if (gui==null)
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
        arglist = new Object[4];
        arglist[0] = new Integer(0);
        arglist[1] = new Integer(0);
        arglist[2] = new Integer(1);
        arglist[3] = new Integer(1);
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
			this.world_folder = world_folder;
			return true;
		}
		else
		{
			printe("World folder does not exist." + newline);
			this.world_folder = null;
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
		chunk_min_x = 0;
		chunk_max_x = 0;
		chunk_min_z = 0;
		chunk_max_z = 0;
		die = false;
		last_percent = 0;
		count_percents = 0;
		
		// Make sure the world folder has been specified.
		if (world_folder == null)
		{
			printe("No world folder specified."+ newline);
			return;
		}
		if (outputDir == null)
		{
			printe("No output dir specified\n");
			return;
		}
		
		//	if((new File(world_folder,"EXTRACTEDBIOMES")).exists() && flush)
			if(outputDir.exists() && flush)
			{
				printm("Deleting existing biome data in " + outputDir.getAbsolutePath() + newline);
				deleteDirectory(outputDir);
			}
			
			printm("Opening " + world_folder.getName() + "..." + newline);
			this.setupDataFolder(world_folder.getAbsolutePath());

			printm("Locating Minecraft save..."+newline);
			if (!this.loadWorld())
			{
				printe("Minecraft classes failed to load the world."+ newline);
				return;
			}
			
			byte[] data = new byte[32768];
			byte[] coords = new byte[2];
			
			printm("Calculating biome values..."+newline);
			
			if (!(outputDir.exists() && outputDir.isDirectory()))
			{	
				if (!outputDir.mkdirs())
				{
					printe("No write access to world folder. Cannot continue."+newline);
					return;
				}
			}
			
			printm("Scanning save folder..." + newline);
			
			this.setupWorldBounds(world_folder, 0);
			printm("World Bounds: ( "+ Integer.toString(chunk_min_x) + " , " + Integer.toString(chunk_min_z) + " ) to ( " + Integer.toString(chunk_max_x) + " , " + Integer.toString(chunk_max_z) + " )" + newline);
			printm("Saving biome data...  (press esc to cancel)" + newline);
			
			for (int x = (chunk_min_x/8-1)*8; x<=((chunk_max_x/8)*8); x=x+8)
			{
				for (int z = (chunk_min_z/8-1)*8; z<=((chunk_max_z/8)*8); z=z+8)
				{
					
					int fromx = x*16;
					int tox = (x+8)*16;
					int fromz = z*16;
					int toz = (z+8)*16;
					
					File biomefile = new File(outputDir, Integer.toString(x)+"."+Integer.toString(z)+".biome");
					
					if (!biomefile.exists())
					{
						for(int i = fromx; i < tox; i++)
						{
							for(int j = fromz; j < toz; j++)
							{	
								coords = getCoordsAtBlock(i,j);
								data[2*(i-fromx)+(j-fromz)*256] = coords[1];
								data[2*(i-fromx)+1+(j-fromz)*256] = coords[0];
							}
						}
					
						// Write out the biome data here.
			            FileOutputStream fileoutputstream;
						try {
							fileoutputstream = new FileOutputStream(biomefile);
							fileoutputstream.write(data, 0, 32768);
				            fileoutputstream.close();
						} catch (Throwable e) {
							printe("A biome file failed to write. Stopping."+newline);
							
							return;
						}
					}
					
					// Progress Report (so the user doesn't get too anxious, this can take forever)
					int iteration = (z-chunk_min_z) + (x-chunk_min_x)*(chunk_max_z-chunk_min_z);
					int total_iteration = (chunk_max_x-chunk_min_x)*(chunk_max_z-chunk_min_z);
					int percent = (int)(((float)iteration/(float)total_iteration)*100);
					if (percent > 100)
						percent = 100;
					if (percent-last_percent >= 1)
					{
						if (count_percents%10 == 0)
							printm(newline);
						printm(Integer.toString((int)(((float)iteration/(float)total_iteration)*100))+"% ... ");
						
						last_percent = (int)(((float)iteration/(float)total_iteration)*100);
						count_percents++;
					}
					if (die)
						break;
				}
				if (die)
					break;
			}
			if (!die)
			{
				
				printm(newline+newline+"Done! This world is now ready to be used with a biome-capable"+newline);
				printm("mapping program such as mcmap. If your boundaries expand, you will need"+newline);
				printm("to come back and re-process. (It will be much faster the second time)"+newline+newline);
				printm("You may now exit or select another world to process."+newline+newline);
				
			}
			else
			{
				
				printm(newline+"Operation safely cancelled (progress saved)."+newline+newline);
				printm("You may now exit or select another world to process."+newline+newline);
				
			}
		}
	
	private void printm(String msg)
	{
		if (!errorsOnly)
		{
			if (useGUI)
				gui.message(msg);
			else
				System.out.print(msg);
		}
				
	}
	
	private void printe(String msg)
	{
		if (useGUI)
			gui.message(msg);
		else
			System.out.print(msg);
	}
	
	public boolean loadWorld()
	{
		// Creating the Minecraft Save object
		Object saveargs[] = new Object[2];
        saveargs[0] = world_folder;
        saveargs[1] = "";
        
		Object saveargs2[] = new Object[4];
        saveargs2[0] = world_folder;
        saveargs2[1] = "";
        saveargs2[2] = 0L;
        saveargs2[3] = null;
      
		try {
			if (isServerJar)
				MinecraftSave = createMinecraftSave.newInstance(saveargs2);
			else
				MinecraftSave = createMinecraftSave.newInstance(saveargs);
		} catch (IllegalArgumentException e1) {
			printe("Minecraft Save object rejected arguments!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (InstantiationException e1) {
			printe("Minecraft Save object failed to instantiate!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (IllegalAccessException e1) {
			printe("Minecraft Save object missing!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (InvocationTargetException e1) {
			printe("Minecraft Save object invocation failed!"+newline);
			printe("Minecraft version was incompatible."+newline);
			return false;
		}
		
		if (loadedField != null)
		{
			try {
				if(! loadedField.getBoolean(MinecraftSave))
				{
					printm("Level Loaded!"+newline);
				}
				else
				{
					printe("Level failed to load!"+newline);
					return false;
				}
			} catch (IllegalArgumentException e2) {
				printe("Couldn't check if save loaded."+newline);
				printm("Things might not work."+newline);
			} catch (IllegalAccessException e2) {
				printe("Couldn't check if save loaded."+newline);
				printm("Things might not work."+newline);
			}
		}
	
		// Creating the Biome Generator using the minecraft save object
		Object biogenargs[] = new Object[1];
		biogenargs[0] = MinecraftSave;
		try {
			BiomeGenerator = createBiomeGenerator.newInstance(biogenargs);
		} catch (IllegalArgumentException e1) {
			printe("BiomeGen constructor rejected arguments!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (InstantiationException e1) {
			printe("BiomeGen object failed to instantiate!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (IllegalAccessException e1) {
			printe("BiomeGen object missing!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (InvocationTargetException e1) {
			printe("BiomeGen object invocation failed!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		}
		return true;
	}

	// Setup the min/max chunk values for the currently selected world
	private void setupWorldBounds(File start,int level)
	{
		level++;
		if (level > 3)
			return;
		File[] listing = start.listFiles();
		String[] parts;
		for(int i=0;i<listing.length;i++)
		{
			if (listing[i].isDirectory() && !listing[i].getName().startsWith(".") && !listing[i].getName().equalsIgnoreCase("DIM-1"))
				setupWorldBounds(listing[i],level);
			else if (listing[i].getName().endsWith(".dat") && level==3)
			{
				// Decode X,Z and place in max,mins
				parts = listing[i].getName().split("\\.");
				if (parts.length>2)
				{
					int x = Integer.parseInt(parts[1], 36);
					int z = Integer.parseInt(parts[2], 36);
					
					if (java.lang.Math.abs(x) < 4096 && java.lang.Math.abs(z)< 4096)
					{
						if (x<chunk_min_x)
							chunk_min_x = x;
						if (x>chunk_max_x)
							chunk_max_x = x;
						if (z<chunk_min_z)
							chunk_min_z = z;
						if (z>chunk_max_z)
							chunk_max_z = z;
						
					}
				}
			}
		}
	}
	
    private static final Class<?>[] parameters = new Class[]{URL.class};
    
    /**
     * Adds the content pointed by the URL to the classpath.
     * @param u the URL pointing to the content to be added
     * @throws IOException
     */
    private static void addJarToClasspath(File jarfile) throws IOException {
    	URL u = jarfile.toURI().toURL();
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u }); 
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }      
    }
    
 // Given a class name, this function creates a string which
    // characterizes the class's structure without including
    // any names.
    private static String generateClassSignature(String classname)
    {   
    	String signature = new String("");
         try {
             Class<?> cls = Class.forName(classname);
          
             Constructor<?> ctorlist[] = cls.getDeclaredConstructors();
                            for (int i = 0; i < ctorlist.length; i++) 
                            {
                                  Constructor<?> ct = ctorlist[i];
                                  Class<?> pvec[] = ct.getParameterTypes();
                                  for (int j = 0; j < pvec.length; j++)
                                	  signature = signature + "carg:" + pvec[j].toString()+" ";
                                  signature = signature + "+";
                               }
             
             
              Method methlist[]  = cls.getDeclaredMethods();
              for (int i = 0; i < methlist.length; i++) {  
                 Method m = methlist[i];
                 Class<?> pvec[] = m.getParameterTypes();
                 for (int j = 0; j < pvec.length; j++)
                 {
                	 if (pvec[j].toString().startsWith("class"))
                		 signature = signature + "arg:class" + " ";
                	 else
                		 signature = signature + "arg:" + pvec[j].toString() + " ";
                 }
            	 if (m.getReturnType().toString().startsWith("class"))
	                 signature = signature + "ret:class" + "+";
            	 else
            		 signature = signature + "ret:" + m.getReturnType().toString() + "+";
              }
                 
                 Field fieldlist[] = cls.getDeclaredFields();
                 for (int i = 0; i < fieldlist.length; i++) 
                 {
                	 Field fld = fieldlist[i];
                	 if (fld.getType().toString().startsWith("class"))
                			 signature = signature + "fld:class" + "+";
                	 else
                		 signature = signature + "fld:" + fld.getType().toString() + "+";
                 }
           }
           catch (Throwable e) {
              //System.err.println(e);
           }
    	return signature;
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
				printm("No write access to world folder. Cannot continue."+newline);
				
				return;
			}
		}
		
		try {
			if (grasscolorimage != null)
			{
				ImageIO.write(grasscolorimage, "png", new File(biomesFolder,"grasscolor.png"));
			}
			if (foliagecolorimage != null)
			{
				ImageIO.write(foliagecolorimage, "png", new File(biomesFolder,"foliagecolor.png"));
			}
		} catch (IOException e) {
			printm("Failed to write biome color pngs. Continuing..."+newline);
		}
    }
    
    // This function attempts to make matching plain fields in a class easier.
    // Basically, you give it a type and a number fcount and if will find the
    // fcount-th field with that type.
    private String getFieldWithType(String classname, String tpe, int fcount)
    {
    	int count = 0;
         try {
        	 Class<?> cls = Class.forName(classname);
             Field fieldlist[] = cls.getDeclaredFields();
             for (int i = 0; i < fieldlist.length; i++) 
             {
            	 Field fld = fieldlist[i];
            	 if (fld.getType().toString().equals(tpe))
            	 {
            		 count++;
            		 if (count == fcount)
            			 return fld.getName();
            	 }
             }
       }
       catch (Throwable e) {
       }
       return "";
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
    
    // A quick recursive function to delete a directory
	// Found it online. Thanks... whoever. I am sorry I forgot
	// your name!
	  static private boolean deleteDirectory(File path) {
		    if( path.exists() ) {
		      File[] files = path.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		           deleteDirectory(files[i]);
		         }
		         else {
		           files[i].delete();
		         }
		      }
		    }
		    return( path.delete() );
		  }
	  
	/*
	 * The manual API
	 */
	  public double getTemperatureAtBlock(int x, int z) // Returns the temperature (double)
	  {
		  	arglist[0] = x;
			arglist[1] = z;
			try {
				generateForLoaction.invoke(BiomeGenerator, arglist);  // BiomeGenerator.a(i,j,1,1);
			} catch (Throwable e1) {
				printe("Could not generate biome vals for coords..."+newline);
			} 
			
			try {
				return ((double[])genTemp.get(BiomeGenerator))[0]; // BiomeGenerator.a[0];
			} catch (Throwable e) {
				printe("Could not extract temp from generator"+newline);
				return 0;
			}
	  }
	  public double getMoistureAtBlock(int x, int z) // Returns the moisture (double)
	  {
		  	arglist[0] = x;
			arglist[1] = z;
			try {
				generateForLoaction.invoke(BiomeGenerator, arglist);  // BiomeGenerator.a(i,j,1,1);
			} catch (Throwable e1) {
				printe("Could not generate biome vals for coords..."+newline);
			} 
			
			try {
				return ((double[])genMoist.get(BiomeGenerator))[0]; // BiomeGenerator.b[0];
			} catch (Throwable e) {
				printe("Could not extract moist from generator"+newline);
				return 0;
			}
	  }
	  public byte[] getCoordsAtBlock(int x, int z) // Returns the location of the biome color in the 256x256 biome PNG (an int)
	  {
		  	arglist[0] = x;
			arglist[1] = z;
			byte[] coords = new byte[2];
			coords[0] = 0;
			coords[1] = 0;
			try {
				generateForLoaction.invoke(BiomeGenerator, arglist);  // BiomeGenerator.a(i,j,1,1);
			} catch (Throwable e1) {
				printe("Could not generate biome vals for coords..."+newline);
			} 
			double temp,moisture;
			try {
				temp = ((double[])genTemp.get(BiomeGenerator))[0]; // BiomeGenerator.a[0];
				moisture = ((double[])genMoist.get(BiomeGenerator))[0]; // BiomeGenerator.b[0];
			} catch (Throwable e) {
				printe("Could not extract temp/moist from generator"+newline);
				return coords;
			}
			
			// Reconstruct the double-to-int function here
			moisture *= temp;
			coords[0] = (byte) ((1.0D - temp) * 255.0D);
			coords[1] = (byte) ((1.0D - moisture) * 255.0D);
			return coords;
	  }
	  public int getRGBAtBlock(int x, int z, int type) // Returns a Color (or an int for efficiency) of the biome color at a given block
	  {
		  	byte[] coords = getCoordsAtBlock(x,z);
		  	//System.out.print("("+Byte.toString(coords[0])+","+Byte.toString(coords[1])+")");
			if (type == 0)
				return grasscolorimage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else if (type == 1)
				return foliagecolorimage.getRGB((int)coords[0]&0xFF, (int)coords[1]&0xFF);
			else
				return 0;
	  }
	  public Color getColorAtBlock(int x, int z, int type) // Returns a Color (or an int for efficiency) of the biome color at a given block
	  {
		  	return new Color(getRGBAtBlock(x,z,type));
	  }
	  
	public boolean setBiomeImages(File grasscolor, File foliagecolor)
	{
			try 
			{
				if (grasscolor != null)
				{
					grasscolorimage = ImageIO.read(grasscolor);
				}
				if (foliagecolor != null)
				{
					foliagecolorimage = ImageIO.read(foliagecolor);
				}
			} 
			catch (IOException e) 
			{
				return false;
			}
		return true;
	}
	
	public void setjarlocation(File mjar)
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
		
		printm("Discovering minecraft.jar interface..."+newline);
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
			printe("Failed to locate minecraft.jar"+newline);
			printe("Path: " + minecraftJar.getAbsolutePath() +newline);
			printe("Minecraft doesn't appear to be installed!"+newline);

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
					grasscolorimage = ImageIO.read(mcjar.getInputStream(grasscolor));
				}
				if (foliagecolor != null)
				{
					foliagecolorimage = ImageIO.read(mcjar.getInputStream(foliagecolor));
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
					
					printm("Removed MOJANG signatures."+newline);
				}
				
			} catch (ZipException e1) {
				System.out.println(currentfile.getName());
				e1.printStackTrace();
                printe("Failed to extract file from zip:" + currentfile.getName() +newline);
				
			} catch (IOException e1) {
				e1.printStackTrace();
                printe("Failed to open new jar for writing!"+newline);
				
			}
		}
		
		try {
			addJarToClasspath(minecraftJar);
		} catch (IOException e) {
			printe("I can't seem to find your minecraft.jar file!"+newline);
		}
		
		// The classes, methods, and fields we need, as strings:
		// Classes
		String save_class = "cu";
		String save_class_signature = saveclass_ref;
		
		String biome_gen_class = "pb";
		String biome_gen_class_signature = biomegenclass_ref;
		
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
			class_signatures.add(generateClassSignature(class_listing.get(i)));
			
			// Dump all the class signatures - helpful!
			//System.out.println(class_listing.get(i));
			//System.out.println(generateClassSignature(class_listing.get(i)));
			//System.out.println("\n");
		}
		
		int class_id = this.matchClassSignature(save_class_signature);
		if (class_id != -1)
		{
			save_class = class_listing.get(class_id);
			printm("Save class is: "+save_class+newline);
		}
		else
		{
			printe(newline + "Deobfuscation of minecraft.jar failed."+newline);
			printe("Signature match for save class not found."+newline);
			printe("Class listing ("+ Integer.toString(class_listing.size()) + "entires)" + newline);
			
			for (int i=0; i<class_listing.size(); i++)
			{
				printe("\t"+ class_listing.get(i) + ".class" + newline);
			}
			return false;
		}
		
		class_id = this.matchClassSignature(biome_gen_class_signature);
		if (class_id != -1)
		{
			biome_gen_class = class_listing.get(class_id);
			printm("Biome Gen class is: "+biome_gen_class+newline);
		}
		else
		{
			printe("Deobfuscation of minecraft.jar failed."+newline);
			printe("Signature match for biome gen class not found."+newline);
			return false;
		}
	
		// Get the classes we need using reflection
		
		try {
			MINECRAFTSAVECLASS = Class.forName(save_class);
			BIOMEGENERATORCLASS = Class.forName(biome_gen_class);
		} catch (ClassNotFoundException e2) {
			printe("Can't find Minecraft! Looked here:"+newline);
			printe(minecraftJar.getAbsolutePath()+newline);
			printe("Make certain minecraft.jar is in that location."+newline);
			return false;
		}
		
		// Setup the MINECRAFTSAVECLASS constructor
		// Client
		Class<?> partypes[] = new Class[2];
        partypes[0] = File.class;
        partypes[1] = String.class;
        
		try {
			createMinecraftSave = MINECRAFTSAVECLASS.getConstructor(partypes);
		} catch (SecurityException e1) {
			printe("Could not bind MinecraftSave Constructor (security issue)"+newline);
			printe("I'm trying to delete the Mojang signatures for you."+newline);
			printe("Restart the program and try again."+newline);
			return false;
		} catch (NoSuchMethodException e1) {
			
			// This could mean that you are operating on a server JAR. Try this:
			try {
				printm("Looks like this might be a server JAR. Keep trying..."+newline);
				partypes = new Class[4];
		        partypes[0] = File.class;
		        partypes[1] = String.class;
		        partypes[2] = Long.TYPE;
				partypes[3] = Class.forName(class_listing.get(this.matchClassSignature(serverrandclass_ref)));
				createMinecraftSave = MINECRAFTSAVECLASS.getConstructor(partypes);
				isServerJar = true;
			} catch (Throwable e) {
				printe("Could not bind MinecraftSave Constructor for servers."+newline);
				printe("Minecraft version was incompatible"+newline);
				return false;
			}
		}
		
        // Setup the BIOMEGENERATORCLASS constructor
		partypes = new Class[1];
        partypes[0] = MINECRAFTSAVECLASS;
        try {
			createBiomeGenerator = BIOMEGENERATORCLASS.getConstructor(partypes);
		} catch (SecurityException e1) {
			printe("Could not bind BiomeGenerator Constructor (security issue)"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (NoSuchMethodException e1) {
			printe("Could not bind BiomeGenerator Constructor"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		}
		
		// Setup the save class loaded field
		save_notloaded = getFieldWithType(save_class, save_notloaded_type, save_notloaded_count);

		try {
			loadedField = MINECRAFTSAVECLASS.getField(save_notloaded);
		} catch (SecurityException e2) {
			printe("Couldn't check if save loaded."+newline);
			printm("Trying anyway..."+newline);
		} catch (NoSuchFieldException e2) {
			printe("Couldn't check if save loaded."+newline);
			printm("Trying anyway..."+newline);
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
			generateForLoaction = BIOMEGENERATORCLASS.getMethod(biome_generator, partypes);
		} catch (SecurityException e1) {
			printe("Failed to setup biogen method!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (NoSuchMethodException e1) {
			printe("Failed to setup biogen method!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		}
		
        // Binding the fields that read out the temperature and moisture
		try {
			genTemp = BIOMEGENERATORCLASS.getField(biome_gen_temp);
			genMoist = BIOMEGENERATORCLASS.getField(biome_gen_moist);
		} catch (SecurityException e1) {
			printe("Failed to setup temp/moist fields!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		} catch (NoSuchFieldException e1) {
			printe("Failed to setup temp/moist fields!"+newline);
			printe("Minecraft version was incompatible"+newline);
			return false;
		}
		printm("Ready!"+newline+newline);
		
		bound = true;
		return true;
	}
	
	// END CLASS DEF
}
