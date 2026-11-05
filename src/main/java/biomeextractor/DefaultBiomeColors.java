package biomeextractor;

import java.awt.image.BufferedImage;

/** Approximate fallback palettes generated without bundled Minecraft textures. */
final class DefaultBiomeColors {
	private DefaultBiomeColors() {}

	static BufferedImage create(WorldProcessor.ColourType type) {
		BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				int rgb;
				if (type == WorldProcessor.ColourType.WaterColour) {
					rgb = 0x4060ff;
				} else {
					// Blend a dry golden green into a wet green, with cooler shades at higher x.
					float warmth = 1 - x / 255f;
					float wetness = 1 - y / 255f;
					int red = Math.round(145 - 65 * wetness + 15 * warmth);
					int green = Math.round(145 + 40 * wetness + 15 * warmth);
					int blue = Math.round(55 + 25 * wetness - 20 * warmth);
					if (type == WorldProcessor.ColourType.FoliageColour) {
						red = red * 3 / 4;
						green = green * 9 / 10;
					}
					rgb = (red << 16) | (green << 8) | blue;
				}
				image.setRGB(x, y, rgb);
			}
		}
		return image;
	}
}
