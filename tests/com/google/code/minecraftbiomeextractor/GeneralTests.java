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
		File worldDir = new File(".", "testdata/World/world");
		File outputDir = new File(".", "testdata/Output");
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
		assertTrue( FileUtils.areIdentical(outputDir, new File(".", "testdata/ExpectedOutput")) );
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
		
		Color colour = worldProcessor.getColorAtBlock(0, 0, WorldProcessor.ColourType.GrassColour);
		assertEquals(119, colour.getRed());
		assertEquals(196, colour.getGreen());
		assertEquals(69, colour.getBlue());
		assertEquals(255, colour.getAlpha());
	}
}
