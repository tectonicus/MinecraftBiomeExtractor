package org.tectonicus.biomeextractor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorldProcessorFlushTest {
	@Test
	void keepsExistingOutputWhenTheWorldCannotLoad(@TempDir Path temp) throws IOException {
		Path world = Files.createDirectory(temp.resolve("world"));
		Path output = Files.createDirectory(temp.resolve("biomes"));
		Path previousExport = Files.write(output.resolve("b.0.0.biome"), new byte[] {1, 2});

		// No JAR is bound, so loading the world fails; flush must not have deleted anything by then.
		try (WorldProcessor processor = new WorldProcessor()) {
			processor.setWorldFolder(world.toFile());
			processor.setOutputDir(output.toFile());
			processor.setCoordinatesOnly(true);
			processor.setFlush(true);
			processor.run();
		}

		assertTrue(Files.exists(previousExport), "flush deleted the previous export before the world loaded");
	}
}
