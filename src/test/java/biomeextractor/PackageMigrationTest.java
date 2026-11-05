package biomeextractor;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageMigrationTest {
	@Test
	void generatesFallbackPalettesWithoutBundledTextures() throws Exception {
		for (WorldProcessor.ColourType type : WorldProcessor.ColourType.values()) {
			BufferedImage image = DefaultBiomeColors.create(type);
			assertNotNull(image);
			assertEquals(256, image.getWidth());
			assertEquals(256, image.getHeight());
			if (type == WorldProcessor.ColourType.WaterColour) {
				assertEquals(0x4060ff, image.getRGB(0, 0) & 0xffffff);
				assertEquals(image.getRGB(0, 0), image.getRGB(255, 255));
			} else {
				assertNotEquals(image.getRGB(0, 0), image.getRGB(255, 255));
			}
		}
		try (WorldProcessor processor = new WorldProcessor()) {
			assertFalse(processor.isBound());
		}
	}

	@Test
	void resolvesClassesInTheRenamedPackageForReflection() {
		String signature = ReflectionUtil.generateClassSignature(ByteCoord.class.getName());
		assertTrue(signature.contains("carg:byte carg:byte"), signature);
		assertEquals("x", ReflectionUtil.getFieldWithType(ByteCoord.class.getName(), "byte", 1));
		assertEquals("y", ReflectionUtil.getFieldWithType(ByteCoord.class.getName(), "byte", 2));
	}
}
