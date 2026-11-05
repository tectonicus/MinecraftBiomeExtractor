# Minecraft Biome Extractor

A library and command-line/Swing application for extracting biome color coordinates
from historical Minecraft worlds. Java classes are in the `biomeextractor` package.

## Build

Install JDK 21 and use the included Gradle wrapper:

```powershell
.\gradlew.bat clean build
```

On macOS/Linux, use `./gradlew clean build`.

The build produces `build/libs/BiomeExtractor-1.0.jar`, an executable JAR with no
external runtime dependencies, and `build/libs/BiomeExtractor-1.0-sources.jar`.
Application distributions and launcher scripts are also available through the
standard `distZip`, `distTar`, and `installDist` tasks.

Production code targets Java 8 bytecode. The Gradle build and unit tests use JDK 21.
Version metadata is generated without requiring Ant or Subversion. Set the optional
`BUILD_NUMBER` environment variable to label a build.

## Run

```powershell
java -jar build/libs/BiomeExtractor-1.0.jar -help
.\gradlew.bat run --args="-help"
```

Extraction uses a dedicated class loader for each processor and works with Java 21
class loading. It automatically tries complete binding profiles based on Beta
1.8.1, Beta 1.3_01, and Alpha v1.2.0_02, validating constructors, fields, and methods
before selecting a profile. Each profile includes its world-loading and climate
API. Similar releases may also match; profile names describe signature baselines
rather than an exact Minecraft version detector.

Minecraft 1.0's official client is verified with the Beta 1.8.1 profile; it does
not require another signature set. Verification covers world loading, rectangular
batch sampling, CLI export, and coordinates read back from exported region files.

The Alpha and Beta 1.3_01 profiles read double-precision climate fields. The Beta
1.8.1 profile calls the newer float-array methods. Single-block values and color coordinates
retain the precision of the selected API; the public bulk temperature/moisture
methods continue to return `float[]`. Batch results use `x + z * width` ordering.
The selected profile is available through `getBindingProfileName()` and is printed
by the command-line application.

Alpha saves are supported through their individual `c.<x>.<z>.dat` files in the
two base-36 hash directories. The scanner also reads `.mcr` region filenames and
deduplicates mixed-format saves, using floor division for negative chunk positions.
Only the selected world's Overworld chunks are scanned; nested dimension and
output directories are excluded. Exports retain the current `b.<x>.<z>.biome`
format: one 524288-byte file per occupied 32-by-32-chunk region, even for Alpha
input. This does not restore the older 8-by-8-chunk cache format.

The Alpha profile constructs worlds directly from a directory and name, without a
save-handler interface. It is verified with Mojang's `a1.2.0` client download,
which identifies itself as **Alpha v1.2.0_02**. The original v1.2.0 client and Alpha
server JARs remain unverified.

Invoke extraction with:

```powershell
java -jar build/libs/BiomeExtractor-1.0.jar -nogui path/to/world -jar path/to/minecraft.jar
```

Normal export requires real 256-by-256 grass and foliage colormaps. It exports
`watercolor.png` only when a real water colormap is available; historical versions
such as the verified Alpha client do not contain one. Any stale water image from a
previous export is removed when no real replacement exists. No approximate palettes
are generated or bundled. A missing required colormap stops export before `-flush`
can delete existing output, and the CLI exits with a nonzero status.

Server JARs remain usable for climate and colormap-coordinate extraction. To write
only `.biome` files without requiring or exporting color images, use:

```powershell
java -jar build/libs/BiomeExtractor-1.0.jar -nogui path/to/world -jar path/to/server.jar -coordsOnly
```

Library calls to `getRGBAtBlock` and `getColorAtBlock` require the requested type's
actual colormap and throw a descriptive `IllegalStateException` when it is missing.
Coordinate and temperature/moisture APIs do not require images. Supply external
maps through `setBiomeImages(grass, foliage, water)`; null arguments mean no override
for that type. Supplied maps must decode as 256-by-256 images. A failed call returns
`false` without replacing previous overrides; a successful call replaces the override
set. Supplied maps take precedence over JAR images and persist across rebinding.
Rebinding discards all images from the previous JAR, preventing stale client colors
from being reused for a server JAR. The API export equivalent of `-coordsOnly` is
`setCoordinatesOnly(true)`.

Library consumers must update imports, for example:

```java
import biomeextractor.WorldProcessor;
```

`WorldProcessor` implements `AutoCloseable`. Use it in a try-with-resources block
to release its Minecraft class loader and temporary unsigned JAR:

```java
try (WorldProcessor processor = new WorldProcessor()) {
    processor.setJarLocation(minecraftJar);
    processor.setWorldFolder(worldDirectory);
    if (!processor.bindToMinecraft() || !processor.loadWorld()) {
        throw new IllegalStateException("Unsupported Minecraft JAR or world");
    }
    byte[] coordinates = processor.getCoordsAtBlock(0, 0);
}
```

Rebinding releases the previous loader, and failed bindings clean up automatically.
Each processor keeps its own Minecraft classes, allowing client and server JARs to
be inspected in the same JVM without modifying the application's class path. JAR
scanning does not initialize Minecraft classes. The source Minecraft JAR is never
modified, and an adjacent `.new` copy is no longer created. Close or rebind a
processor only after its extraction thread has stopped.

## Tests

Both test suites use JUnit Jupiter 6.1.3 on JDK 21. `test` runs unit tests. The
historical Minecraft integration suite lives under `src/integrationTest/java`.
Normal builds compile it, but do not run it. Run it explicitly with:

```powershell
.\gradlew.bat integrationTest
```

The integration task copies the checked-in world and numeric `.biome` fixtures into
`build/integration-test/testdata`, excluding JARs and PNGs. Fixture writes do not
modify source files. CLI tests run in a subprocess so `System.exit` cannot terminate
JUnit. Minecraft JARs and textures are never included in the test suite or downloaded
by tests. Supply your own local JARs with absolute paths:

```powershell
$env:MINECRAFT_BETA_13_JAR = "C:\path\to\minecraft-beta-1.3_01.jar"
$env:MINECRAFT_BETA_13_SERVER_JAR = "C:\path\to\minecraft-beta-1.3-server.jar"
$env:MINECRAFT_BETA_181_JAR = "C:\path\to\minecraft-beta-1.8.1.jar"
$env:MINECRAFT_1_0_JAR = "C:\path\to\minecraft-1.0.jar"
$env:MINECRAFT_ALPHA_120_JAR = "C:\path\to\minecraft-alpha-1.2.0_02.jar"
.\gradlew.bat integrationTest
```

Missing variables skip their dependent cases; configured invalid paths fail. The
client/server rebinding cases require both Beta 1.3 paths; cross-version cases also
require the Beta client/server they switch to. Gradle tracks paths and JAR contents,
so adding or changing a JAR reruns the suite. Keep local JARs outside the checkout,
or in the ignored `testdata` location, and exclude them from CI artifact uploads.

Coverage includes exact exported `.biome` bytes, decoded colormap pixels read
straight from the supplied client JAR, rectangular climate queries, recovery and
rebinding. Server cases verify coordinate-only export and clear failures for RGB
or normal image export without real maps, including preservation of existing output
when `-flush` is supplied. Tests also cover explicit maps, malformed images, clearing
JAR images on rebinding, and omission of nonexistent water palettes. All unit-test
images are generated test patterns, not Minecraft assets. The Alpha case also verifies individual-chunk discovery.
Scanner unit tests cover negative boundaries, base-36 names, deduplication,
malformed files, and dimension exclusion without Minecraft JARs.

The currently verified Alpha client is v1.2.0_02. Beta 1.8.1 and Minecraft 1.0 client
paths have also been verified; their real server constructor paths remain unverified.

## Binding profiles

Reference signatures live in `src/main/resources/biomeextractor/signatures`.
`BindingProfile` registers each set and its API generation. `MinecraftBindings`
matches classes structurally and validates a complete set before it becomes
active. Failed candidates leave no partial bindings, and another profile can be
tried using the same isolated loader. Unsupported JARs return `false` from
`bindToMinecraft()`.

To support another release with the same API generation, add a signature resource
and a `BindingProfile` entry, plus a regression fixture. Releases that change their
constructor or climate API also require a corresponding adapter in
`MinecraftBindings`.
