package com.google.code.minecraftbiomeextractor;

import java.awt.Color;
import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.code.minecraftbiomeextractor.MinecraftBiomeExtractor;

public class GeneralTests extends TestCase
{
	@Test
	public void testBasic()
	{
		File worldDir = new File(".", "testdata/RegionWorld/world");
		File outputDir = new File(".", "testdata/RegionOutput");
		File minecraftClient = new File(".", "testdata/minecraft.jar");
		
		// Delete existing output dir
		FileUtils.deleteDirectory(outputDir);
		
		String[] args =
		{
			"-noGui",
			worldDir.getAbsolutePath(),
			"-outputDir",
			outputDir.getAbsolutePath(),
			"-jar",
			minecraftClient.getAbsolutePath()
		};
		
		MinecraftBiomeExtractor.main(args);
		
		// Check our output is identical to the canned output from earlier
		final boolean areIdentical = FileUtils.areIdentical(outputDir, new File(".", "testdata/ExpectedRegionOutput")); 
		assertTrue(areIdentical);
	}
	
	@Test
	public void testBasicServer()
	{
		File worldDir = new File(".", "testdata/RegionWorld/world");
		File outputDir = new File(".", "testdata/RegionOutput");
		File minecraftClient = new File(".", "testdata/minecraft_server.jar");
		
		// Delete existing output dir
		FileUtils.deleteDirectory(outputDir);
		
		String[] args =
		{
			"-noGui",
			worldDir.getAbsolutePath(),
			"-outputDir",
			outputDir.getAbsolutePath(),
			"-jar",
			minecraftClient.getAbsolutePath()
		};
		
		MinecraftBiomeExtractor.main(args);
		
		// Check our output is identical to the canned output from earlier
		final boolean areIdentical = FileUtils.areIdentical(outputDir, new File(".", "testdata/ExpectedRegionOutput"));
		System.out.println("Identical: "+areIdentical);
		assertTrue(areIdentical);
	}
	
	@Test
	public void testBasicApi()
	{
		File worldDir = new File(".", "testdata/World/world");
		File minecraftClient = new File(".", "testdata/minecraft.jar");
		
		WorldProcessor worldProcessor = new WorldProcessor();
		worldProcessor.setJarLocation(minecraftClient);
		worldProcessor.setWorldFolder(worldDir);
		
		final boolean bindOk = worldProcessor.bindToMinecraft();
		assertTrue(bindOk);
		
		final boolean worldOk = worldProcessor.loadWorld();
		assertTrue(worldOk);
		
		final double moisture00 = worldProcessor.getMoistureAtBlock(0, 0);
		final double moisture48 = worldProcessor.getMoistureAtBlock(4, 8);
		
		assertEquals(0.6294117825226543, moisture00);
		assertEquals(0.6794504557750403, moisture48);
		
		Color colour = worldProcessor.getColorAtBlock(0, 0, WorldProcessor.ColourType.GrassColour);
		assertEquals(119, colour.getRed());
		assertEquals(196, colour.getGreen());
		assertEquals(69, colour.getBlue());
		assertEquals(255, colour.getAlpha());
	}
	
	@Test
	public void testBasicApiWithServer()
	{
		File worldDir = new File(".", "testdata/World/world");
		File minecraftClient = new File(".", "testdata/minecraft_server.jar");
		
		WorldProcessor worldProcessor = new WorldProcessor();
		worldProcessor.setJarLocation(minecraftClient);
		worldProcessor.setWorldFolder(worldDir);
		
		final boolean bindOk = worldProcessor.bindToMinecraft();
		assertTrue(bindOk);
		
		final boolean worldOk = worldProcessor.loadWorld();
		assertTrue(worldOk);
		
		final double moisture00 = worldProcessor.getMoistureAtBlock(0, 0);
		final double moisture48 = worldProcessor.getMoistureAtBlock(4, 8);
		
		assertEquals(0.6294117825226543, moisture00);
		assertEquals(0.6794504557750403, moisture48);
		
	/*
		// TODO: Default images not the same as latest minecraft images?
		Color colour = worldProcessor.getColorAtBlock(0, 0, WorldProcessor.ColourType.GrassColour);
		assertEquals(119, colour.getRed());
		assertEquals(196, colour.getGreen());
		assertEquals(69, colour.getBlue());
		assertEquals(255, colour.getAlpha());
	*/
	}
}
