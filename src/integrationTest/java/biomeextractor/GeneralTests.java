package biomeextractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class GeneralTests {
	@TempDir
	Path temporaryDirectory;

	@Test
	void testBasic() throws Exception {
		assertCommandLineExtraction("MINECRAFT_BETA_13_JAR");
	}

	@Test
	void testBasicServer() throws Exception {
		assertCommandLineExtraction("MINECRAFT_BETA_13_SERVER_JAR");
	}

	@Test
	void testBasicApi() throws Exception {
		try (WorldProcessor worldProcessor = loadWorld("MINECRAFT_BETA_13_JAR")) {
			assertMoistures(worldProcessor);
			Color colour = worldProcessor.getColorAtBlock(0, 0, WorldProcessor.ColourType.GrassColour);
			assertEquals(119, colour.getRed());
			assertEquals(196, colour.getGreen());
			assertEquals(69, colour.getBlue());
			assertEquals(255, colour.getAlpha());
		}
	}

	@Test
	void testBasicApiWithServer() throws Exception {
		try (WorldProcessor worldProcessor = loadWorld("MINECRAFT_BETA_13_SERVER_JAR")) {
			assertMoistures(worldProcessor);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"MINECRAFT_BETA_13_JAR", "MINECRAFT_BETA_13_SERVER_JAR"})
	void matchesIndividualSamplesAcrossRectanglesAndTileBoundaries(String jarName) throws Exception {
		try (WorldProcessor processor = loadWorld(jarName)) {
			assertEquals("Beta 1.3_01", processor.getBindingProfileName());
			assertBatchMatchesSingles(processor, 19, 5);
			assertBatchMatchesSingles(processor, 3, 19);
		}
	}

	@Test
	void canRecoverFromAFailedBindingAndSwitchBetweenClientAndServer() throws Exception {
		try (WorldProcessor processor = loadWorld("MINECRAFT_BETA_13_JAR")) {
			processor.setJarLocation(temporaryDirectory.resolve("missing.jar").toFile());
			assertFalse(processor.bindToMinecraft());
			assertFalse(processor.isBound());
			assertNull(processor.getBindingProfileName());
			bindWorld(processor, fixture("MINECRAFT_BETA_13_SERVER_JAR"));
			assertEquals("Beta 1.3_01", processor.getBindingProfileName());
			assertMoistures(processor);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"MINECRAFT_BETA_181_JAR", "MINECRAFT_1_0_JAR"})
	void supportsTheNewerProfileAndCanSwitchBackToTheOlderApi(String environmentVariable) throws Exception {
		String jar = System.getenv(environmentVariable);
		assumeTrue(jar != null && !jar.isEmpty(), "Set " + environmentVariable + " to test this Minecraft release");
		try (WorldProcessor processor = new WorldProcessor()) {
			bindWorld(processor, new File(jar));
			assertEquals("Beta 1.8.1", processor.getBindingProfileName());
			assertBatchMatchesSingles(processor, 19, 5);
			assertBatchMatchesSingles(processor, 3, 19);
			bindWorld(processor, fixture("MINECRAFT_BETA_13_JAR"));
			assertEquals("Beta 1.3_01", processor.getBindingProfileName());
			assertMoistures(processor);
			assertBatchMatchesSingles(processor, 3, 19);
		}
	}

	@Test
	void supportsAlphaChunkExportAndCanSwitchWorldLoadingApis() throws Exception {
		String jar = System.getenv("MINECRAFT_ALPHA_120_JAR");
		assumeTrue(jar != null && !jar.isEmpty(), "Set MINECRAFT_ALPHA_120_JAR to test Alpha extraction");
		try (WorldProcessor processor = new WorldProcessor()) {
			bindWorld(processor, new File(jar));
			assertEquals("Alpha v1.2.0_02", processor.getBindingProfileName());
			assertBatchMatchesSingles(processor, 19, 5);
			assertBatchMatchesSingles(processor, 3, 19);
			Path output = temporaryDirectory.resolve("alpha-biomes");
			processor.setOutputDir(output.toFile());
			processor.run();
			assertEquals(List.of("b.0.-1.biome", "b.0.0.biome", "b.1.-1.biome", "b.1.0.biome"), regionFiles(output));
			for (int regionX = 0; regionX <= 1; regionX++) {
				for (int regionZ = -1; regionZ <= 0; regionZ++) {
					byte[] exported = Files.readAllBytes(output.resolve("b." + regionX + "." + regionZ + ".biome"));
					assertEquals(524288, exported.length);
					for (int x : new int[] {0, 15, 16, 511}) {
						for (int z : new int[] {0, 15, 16, 511}) {
							byte[] coordinate = processor.getCoordsAtBlock(regionX * 512 + x, regionZ * 512 + z);
							int index = 2 * (x + z * 512);
							assertEquals(coordinate[1], exported[index]);
							assertEquals(coordinate[0], exported[index + 1]);
						}
					}
				}
			}
			for (String type : new String[] {"grass", "foliage", "water"}) {
				BufferedImage image = ImageIO.read(output.resolve(type + "color.png").toFile());
				assertEquals(256, image.getWidth());
				assertEquals(256, image.getHeight());
			}
			bindWorld(processor, fixture("MINECRAFT_BETA_13_SERVER_JAR"));
			assertEquals("Beta 1.3_01", processor.getBindingProfileName());
			assertMoistures(processor);
			bindWorld(processor, new File(jar));
			assertEquals("Alpha v1.2.0_02", processor.getBindingProfileName());
			assertBatchMatchesSingles(processor, 3, 19);
		}
	}

	private void assertCommandLineExtraction(String jarName) throws Exception {
		File worldDir = temporaryDirectory.resolve("region-world").toFile();
		FileUtils.copyFiles(new File("testdata/RegionWorld/world"), worldDir, new HashSet<>());
		File minecraftJar = fixture(jarName);
		File outputDir = temporaryDirectory.resolve("biomes").toFile();
		Path log = temporaryDirectory.resolve("extractor.log");
		String executable = File.separatorChar == '\\' ? "java.exe" : "java";
		String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
		Path application = Path.of(MinecraftBiomeExtractor.class.getProtectionDomain()
				.getCodeSource().getLocation().toURI());
		String classpath = application.toString();
		if (Files.isDirectory(application)) {
			classpath += File.pathSeparator + Path.of(WorldProcessor.class
					.getResource("/biomeextractor/signatures/beta-1.3_01.properties").toURI())
					.getParent().getParent().getParent();
		}

		Process process = new ProcessBuilder(java, "-Djava.awt.headless=true", "-cp",
				classpath, MinecraftBiomeExtractor.class.getName(),
				"-nogui", worldDir.getPath(), "-outputDir", outputDir.getPath(), "-jar", minecraftJar.getPath())
				.redirectErrorStream(true)
				.redirectOutput(log.toFile())
				.start();
		try {
			assertTrue(process.waitFor(60, TimeUnit.SECONDS), "Extraction timed out");
			String output = Files.readString(log);
			assertEquals(0, process.exitValue(), output);
			assertTrue(outputDir.isDirectory(), output);
			Path expected = Path.of("testdata/ExpectedRegionOutput");
			List<String> expectedRegions = regionFiles(expected);
			assertEquals(expectedRegions, regionFiles(outputDir.toPath()), output);
			for (String name : expectedRegions) {
				assertArrayEquals(Files.readAllBytes(expected.resolve(name)),
						Files.readAllBytes(outputDir.toPath().resolve(name)), name);
			}
			for (String type : new String[] {"grass", "foliage", "water"}) {
				BufferedImage reference;
				try (ZipFile jar = new ZipFile(minecraftJar)) {
					var entry = jar.getEntry("misc/" + type + "color.png");
					if (entry != null) {
						try (var input = jar.getInputStream(entry)) { reference = ImageIO.read(input); }
					} else {
						reference = DefaultBiomeColors.create(WorldProcessor.ColourType.valueOf(
								type.substring(0, 1).toUpperCase() + type.substring(1) + "Colour"));
					}
				}
				BufferedImage actual = ImageIO.read(new File(outputDir, type + "color.png"));
				assertEquals(reference.getWidth(), actual.getWidth());
				assertEquals(reference.getHeight(), actual.getHeight());
				assertArrayEquals(reference.getRGB(0, 0, reference.getWidth(), reference.getHeight(), null, 0, reference.getWidth()),
						actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()), type + " colors");
			}
		} finally {
			if (process.isAlive()) {
				process.destroyForcibly();
				process.waitFor(5, TimeUnit.SECONDS);
			}
		}
	}

	private WorldProcessor loadWorld(String jarName) throws Exception {
		File jar = fixture(jarName);
		WorldProcessor worldProcessor = new WorldProcessor();
		boolean loaded = false;
		try {
			bindWorld(worldProcessor, jar);
			loaded = true;
			return worldProcessor;
		} finally {
			if (!loaded) {
				worldProcessor.close();
			}
		}
	}

	private static File fixture(String variable) {
		String path = System.getenv(variable);
		assumeTrue(path != null && !path.isBlank(), "Set " + variable + " to a local Minecraft JAR");
		File jar = new File(path).getAbsoluteFile();
		assertTrue(jar.isFile(), variable + " does not point to a file: " + jar);
		return jar;
	}

	private void bindWorld(WorldProcessor processor, File jar) throws Exception {
		Path world = Files.createTempDirectory(temporaryDirectory, "world-");
		FileUtils.copyFiles(new File("testdata/World/world"), world.toFile(), new HashSet<>());
		processor.setJarLocation(jar);
		assertTrue(processor.setWorldFolder(world.toFile()));
		assertTrue(processor.bindToMinecraft(), "Could not bind to " + jar);
		assertTrue(processor.loadWorld(), "Could not load world with " + jar);
	}

	private static List<String> regionFiles(Path directory) throws Exception {
		try (Stream<Path> paths = Files.list(directory)) {
			return paths.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".biome"))
					.sorted().collect(Collectors.toList());
		}
	}

	private static void assertBatchMatchesSingles(WorldProcessor processor, int width, int depth) throws Exception {
		ByteCoord[] destination = new ByteCoord[width * depth];
		ByteCoord[] bulk = processor.getCoordsAtBlocks(destination, null, null, -17, 33, width, depth);
		assertSame(destination, bulk);
		float[] temperatures = new float[width * depth];
		float[] moistures = new float[width * depth];
		assertSame(temperatures, processor.getTemperaturesAtBlocks(temperatures, -17, 33, width, depth));
		assertSame(moistures, processor.getMoisturesAtBlocks(moistures, -17, 33, width, depth));
		for (int z = 0; z < depth; z++) {
			for (int x = 0; x < width; x++) {
				int index = x + z * width;
				byte[] single = processor.getCoordsAtBlock(-17 + x, 33 + z);
				assertEquals(single[0], bulk[index].x);
				assertEquals(single[1], bulk[index].y);
				assertEquals(processor.getTemperatureAtBlock(-17 + x, 33 + z), temperatures[index], 1e-6);
				assertEquals(processor.getMoistureAtBlock(-17 + x, 33 + z), moistures[index], 1e-6);
			}
		}
	}

	private static void assertMoistures(WorldProcessor worldProcessor) throws Exception {
		assertEquals(0.6294117825226543, worldProcessor.getMoistureAtBlock(0, 0));
		assertEquals(0.6794504557750403, worldProcessor.getMoistureAtBlock(4, 8));
	}
}
