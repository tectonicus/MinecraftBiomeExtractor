package biomeextractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldRegionScannerTest {
	@TempDir
	Path world;

	@Test
	void decodesAlphaCoordinatesAndUsesFloorDivisionForNegativeChunks() throws Exception {
		chunk(-33, -32);
		chunk(-32, -1);
		chunk(-1, 0);
		chunk(0, -1);
		chunk(31, 31);
		chunk(32, 32);
		chunk(5000, -5000);
		assertEquals(List.of("-2,-1", "-1,-1", "-1,0", "0,-1", "0,0", "1,1", "156,-157"), regions());
	}

	@Test
	void mergesMixedSavesAndDeduplicatesRegions() throws Exception {
		chunk(0, 0);
		chunk(1, 1);
		chunk(32, -1);
		file("region/r.0.0.mcr");
		file("region/r.-2.3.mcr");
		assertEquals(List.of("-2,3", "0,0", "1,-1"), regions());
	}

	@Test
	void ignoresUnrelatedFilesMalformedCoordinatesAndOtherDimensions() throws Exception {
		file("level.dat");
		file("DIM-1/0/0/c.0.0.dat");
		file("DIM1/0/0/c.0.0.dat");
		file("biomes/0/0/c.0.0.dat");
		file("0/0/c.bad_name.0.dat");
		file("0/0/c.zzzzzzzzzzzzzzzz.0.dat");
		file("0/0/c.0.0.dat.bak");
		file("region/r.no.0.mcr");
		file("region/r.99999999999999.0.mcr");
		file("region/r.0.0.mca");
		Files.createDirectories(world.resolve("0/0/c.0.0.dat"));
		assertEquals(List.of(), regions());
		chunk(33, 0);
		assertEquals(List.of("1,0"), regions());
	}

	@Test
	void emptyWorldsHaveNoRegionsAndMissingWorldsReportAnIoError() throws Exception {
		assertEquals(List.of(), regions());
		assertThrows(java.io.IOException.class, () -> WorldRegionScanner.scan(world.resolve("missing").toFile()));
	}

	private void chunk(int x, int z) throws Exception {
		file(Integer.toString(x & 63, 36) + "/" + Integer.toString(z & 63, 36)
				+ "/c." + Integer.toString(x, 36) + "." + Integer.toString(z, 36) + ".dat");
	}

	private void file(String relative) throws Exception {
		Path file = world.resolve(relative);
		Files.createDirectories(file.getParent());
		Files.createFile(file);
	}

	private List<String> regions() throws Exception {
		return WorldRegionScanner.scan(world.toFile()).stream()
				.map(region -> region[0] + "," + region[1]).collect(Collectors.toList());
	}
}
