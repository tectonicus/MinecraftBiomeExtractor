package biomeextractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BiomeColorMapsTest {
	@TempDir Path directory;

	@ParameterizedTest
	@EnumSource(WorldProcessor.ColourType.class)
	void missingColorMapsFailWithAnActionableError(WorldProcessor.ColourType type) throws Exception {
		try (WorldProcessor processor = new FixedCoordinates()) {
			IllegalStateException rgb = assertThrows(IllegalStateException.class,
					() -> processor.getRGBAtBlock(0, 0, type));
			IllegalStateException color = assertThrows(IllegalStateException.class,
					() -> processor.getColorAtBlock(0, 0, type));
			assertTrue(rgb.getMessage().contains("colormap"));
			assertTrue(rgb.getMessage().contains("setBiomeImages"));
			assertEquals(rgb.getMessage(), color.getMessage());
		}
	}

	@Test
	void usesTheProvidedPixelsForEachColorIncludingWaterAndUnsignedCoordinates() throws Exception {
		File grass = image("grass.png", 0xff123456);
		File foliage = image("foliage.png", 0xffabcdef);
		File water = image("water.png", 0xff02468a);
		try (WorldProcessor processor = new FixedCoordinates()) {
			assertTrue(processor.setBiomeImages(grass, foliage, water));
			assertEquals(0xff123456, processor.getRGBAtBlock(0, 0, WorldProcessor.ColourType.GrassColour));
			assertEquals(0xffabcdef, processor.getRGBAtBlock(0, 0, WorldProcessor.ColourType.FoliageColour));
			assertEquals(0xff02468a, processor.getRGBAtBlock(0, 0, WorldProcessor.ColourType.WaterColour));
		}
	}

	@Test
	void partialMapsDoNotInventMissingColorsAndInvalidInputsDoNotReplaceValidMaps() throws Exception {
		File grass = image("grass.png", 0xff123456);
		Path invalid = directory.resolve("not-an-image.png");
		Files.writeString(invalid, "Not a PNG");
		Path small = directory.resolve("too-small.png");
		ImageIO.write(new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB), "png", small.toFile());
		try (WorldProcessor processor = new FixedCoordinates()) {
			assertTrue(processor.setBiomeImages(grass));
			assertThrows(IllegalStateException.class, () -> processor.getRGBAtBlock(0, 0, WorldProcessor.ColourType.FoliageColour));
			assertFalse(processor.setBiomeImages(invalid.toFile()));
			assertFalse(processor.setBiomeImages(small.toFile()));
			assertEquals(0xff123456, processor.getRGBAtBlock(0, 0, WorldProcessor.ColourType.GrassColour));
		}
	}

	@Test
	void validatesImageRequirementsBeforeFlushingAnExistingExport() throws Exception {
		Path world = Files.createDirectory(directory.resolve("world"));
		Path output = Files.createDirectory(directory.resolve("output"));
		Path marker = Files.writeString(output.resolve("existing.biome"), "keep");
		try (WorldProcessor processor = new WorldProcessor()) {
			assertTrue(processor.setWorldFolder(world.toFile()));
			processor.setOutputDir(output.toFile());
			processor.setFlush(true);
			IllegalStateException error = assertThrows(IllegalStateException.class, processor::run);
			assertTrue(error.getMessage().contains("grasscolor.png"));
			assertEquals("keep", Files.readString(marker));
			assertFalse(Files.exists(output.resolve("grasscolor.png")));
		}
	}

	private File image(String name, int rgb) throws Exception {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		image.setRGB(255, 128, rgb);
		File file = directory.resolve(name).toFile();
		assertTrue(ImageIO.write(image, "png", file));
		return file;
	}

	private static class FixedCoordinates extends WorldProcessor {
		@Override public byte[] getCoordsAtBlock(int x, int z) { return new byte[] {(byte)255, (byte)128}; }
	}
}
