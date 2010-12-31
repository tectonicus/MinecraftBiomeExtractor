package com.google.code.minecraftbiomeextractor;

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
}
