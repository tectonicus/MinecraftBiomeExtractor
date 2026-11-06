# Minecraft Biome Extractor

Extract biome colormap coordinates from historical Minecraft worlds through a Java API, CLI, or Swing interface. Requires **Java 21+**; builds use JDK 21.

Developed for early Minecraft mapping tools, the extractor reconstructs biome-dependent grass and foliage colors for worlds created before Minecraft stored biome IDs in chunks. It runs the matching Minecraft JAR's generator using the world seed, producing per-block colormap coordinates. Mapping tools use those coordinates with the client colormaps to color their rendered maps. Standalone exports are `.biome` files and PNGs; applications can also query coordinates directly through the Java API.

Historical users included **mcmap**, **MCMap Live**, **Minemapper**, **Minecraft Overviewer** and **Tectonicus**.

## Build

```powershell
.\gradlew.bat build
```

Use `./gradlew` on macOS/Linux. The executable JAR is `build/libs/BiomeExtractor-1.1.0.jar`; a sources JAR is also produced.

## Usage

```powershell
# Export coordinates and available real colormaps
java -jar build/libs/BiomeExtractor-1.1.0.jar -nogui path/to/world -jar path/to/client.jar

# Export coordinates only, including with supported server JARs
java -jar build/libs/BiomeExtractor-1.1.0.jar -nogui path/to/world -jar path/to/server.jar -coordsOnly
```

Omit `-nogui` to open the GUI. Use `-help` for options and `-flush` to regenerate existing output.

Image export requires real 256×256 grass and foliage colormaps. Water is exported only when its colormap exists. Missing required maps stop export before flushing existing files. No Minecraft assets are bundled. The original repo's history was rewritten to remove Minecraft jars and colormap pngs.

Alpha chunk files and MCRegion (`.mcr`) saves are supported. Only Overworld chunks are scanned. Output is one 524288-byte `b.<x>.<z>.biome` file per occupied 32×32-chunk region.

## Compatibility

**Intended version range: Alpha v1.2.0 through snapshot 12w06a, inclusive.** This covers the remaining Alpha releases, Beta releases, release 1.0 and 1.1, and the early 1.2 development snapshots before Anvil.

Snapshot **12w07a** introduced Anvil (`.mca`) and saved biome data; **1.2.1** was the first stable Anvil release. For Anvil worlds, mapping tools should read the stored biomes directly. See the [Anvil format history](https://minecraft.wiki/w/Anvil_file_format).

The intended range includes builds that have not yet been tested or may require additional binding profiles. Verified compatibility is listed below.

Verified clients: **Alpha v1.2.0_02, Beta 1.3_01, Beta 1.8.1, and release 1.0**. The Beta 1.3-era server jar is also verified for coordinate extraction.

Other server versions remain unverified. Similar releases may match a binding profile, but profile names are not exact version detection. Use the JAR matching the world's generator.

## Java API

```java
import biomeextractor.WorldProcessor;

try (WorldProcessor processor = new WorldProcessor()) {
    processor.setJarLocation(minecraftJar);
    processor.setWorldFolder(worldDirectory);
    if (!processor.bindToMinecraft() || !processor.loadWorld()) {
        throw new IllegalStateException("Unsupported Minecraft JAR or world");
    }
    byte[] coordinates = processor.getCoordsAtBlock(0, 0);
}
```

`getSwampMaskAtBlocks(x, z, width, depth)` returns a row-major mask of the generated Beta 1.8+ swamp biome, for consumers applying historical swamp tints. Earlier profiles return an all-false mask.

Each processor owns an isolated class loader and temporary unsigned JAR. Close it after extraction stops; the source JAR is never modified. Bulk results use `x + z * width` ordering.

`getRGBAtBlock` and `getColorAtBlock` require the requested colormap and throw `IllegalStateException` if it is missing. Coordinate and climate queries need no images. Supply external maps with `setBiomeImages(grass, foliage, water)`; null means no override. Invalid maps return `false`. Successful calls replace the overrides, which persist across rebinding. Use `setCoordinatesOnly(true)` for image-free exports.

## Tests

Tests use JUnit 6. `build` runs unit tests and compiles integration tests. To run integration tests, supply local JARs with absolute paths:

```powershell
$env:MINECRAFT_ALPHA_120_JAR = "C:\jars\minecraft-alpha-1.2.0_02.jar"
$env:MINECRAFT_BETA_13_JAR = "C:\jars\minecraft-beta-1.3_01.jar"
$env:MINECRAFT_BETA_13_SERVER_JAR = "C:\jars\minecraft-beta-1.3-server.jar"
$env:MINECRAFT_BETA_181_JAR = "C:\jars\minecraft-beta-1.8.1.jar"
$env:MINECRAFT_1_0_JAR = "C:\jars\minecraft-1.0.jar"
.\gradlew.bat integrationTest
```

Missing variables skip dependent test cases; invalid paths fail. Tests never download Minecraft assets. Keep JARs outside the checkout or in ignored `testdata`, and exclude them from CI artifacts. World fixtures are copied into `build` before use.

## Adding versions

Binding profiles identify Minecraft classes by their constructors, methods, and fields rather than obfuscated class names. To support another release:

1. **Try an existing profile first.** Bind the new JAR and check `getBindingProfileName()`. A successful match still needs verification of world loading and generated coordinates; release 1.0, for example, works with the Beta 1.8.1 profile.
2. **Identify changed classes.** Inspect the JAR with a decompiler, then use `ReflectionUtil.generateClassSignature(className, loader)` with that JAR's class loader. Compare the world, biome generator, and any save-handler, world-info, or server-dimension classes against the closest existing profile.
3. **Register new signatures when needed.** Copy the closest file in `src/main/resources/biomeextractor/signatures` and update the affected roles. Add a `BindingProfile` entry selecting the world-loading API (`DIRECT`, `SAVE_HANDLER`, or `WORLD_INFO`) and whether climate values use double fields or float-array methods. New constructor or climate APIs also require changes in `MinecraftBindings`.
4. **Add integration coverage.** Supply the JAR through a new environment variable and register it as a Gradle integration-test input. Verify negative coordinates, rectangular batches, exported coordinates, and real colormap pixels where available. Test client and server variants separately, then run `gradlew.bat build integrationTest`.

Document the versions actually verified. Keep game JARs and textures external; use world fixtures and numeric expectations for regression tests.
