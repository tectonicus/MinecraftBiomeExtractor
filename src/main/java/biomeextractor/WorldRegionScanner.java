package biomeextractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds occupied 32-by-32-chunk regions in McRegion and individual-chunk saves. */
final class WorldRegionScanner {
	private static final Pattern REGION = Pattern.compile("r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mcr");
	private static final Pattern CHUNK = Pattern.compile("c\\.(-?[0-9a-z]+)\\.(-?[0-9a-z]+)\\.dat", Pattern.CASE_INSENSITIVE);
	private static final Pattern HASH_DIRECTORY = Pattern.compile("[0-9a-z]{1,2}", Pattern.CASE_INSENSITIVE);

	private WorldRegionScanner() {
	}

	static List<int[]> scan(File world) throws IOException {
		Set<Long> regions = new HashSet<>();
		File regionDirectory = new File(world, "region");
		if (regionDirectory.isDirectory()) {
			for (File file : entries(regionDirectory)) {
				Matcher name = REGION.matcher(file.getName());
				if (file.isFile() && name.matches()) {
					add(regions, name, 10, false);
				}
			}
		}
		for (File first : entries(world)) {
			if (!isHashDirectory(first)) {
				continue;
			}
			for (File second : entries(first)) {
				if (!isHashDirectory(second)) {
					continue;
				}
				for (File file : entries(second)) {
					Matcher name = CHUNK.matcher(file.getName());
					if (file.isFile() && name.matches()) {
						add(regions, name, 36, true);
					}
				}
			}
		}
		List<int[]> result = new ArrayList<>();
		for (long key : regions) {
			result.add(new int[] {(int)(key >> 32), (int)key});
		}
		result.sort(Comparator.comparingInt((int[] region) -> region[0]).thenComparingInt(region -> region[1]));
		return result;
	}

	private static void add(Set<Long> regions, Matcher name, int radix, boolean chunkCoordinates) {
		try {
			int x = Integer.parseInt(name.group(1), radix);
			int z = Integer.parseInt(name.group(2), radix);
			if (chunkCoordinates) {
				x = Math.floorDiv(x, 32);
				z = Math.floorDiv(z, 32);
			}
			regions.add(((long)x << 32) | (z & 0xffffffffL));
		} catch (NumberFormatException ignored) {
			// Ignore unrelated or corrupt filenames instead of losing valid chunks.
		}
	}

	private static boolean isHashDirectory(File directory) {
		return directory.isDirectory() && !Files.isSymbolicLink(directory.toPath())
				&& HASH_DIRECTORY.matcher(directory.getName()).matches()
				&& Integer.parseInt(directory.getName(), 36) < 64;
	}

	private static File[] entries(File directory) throws IOException {
		File[] files = directory.listFiles();
		if (files == null) {
			throw new IOException("Could not list world directory: " + directory);
		}
		return files;
	}
}
