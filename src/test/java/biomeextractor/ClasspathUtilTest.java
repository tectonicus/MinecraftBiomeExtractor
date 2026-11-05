package biomeextractor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClasspathUtilTest {
	@TempDir
	Path directory;

	@Test
	void isolatesDifferentJarsWithoutChangingTheApplicationClasspath() throws Exception {
		Path firstJar = createJar("first", "public String value() { return \"first\"; }");
		Path secondJar = createJar("second", "public String value() { return \"second\"; }");
		try (URLClassLoader first = ClasspathUtil.createClassLoader(firstJar.toFile());
				URLClassLoader second = ClasspathUtil.createClassLoader(secondJar.toFile())) {
			Class<?> firstType = first.loadClass("isolated.Probe");
			Class<?> secondType = second.loadClass("isolated.Probe");
			assertNotSame(firstType, secondType);
			assertSame(first, firstType.getClassLoader());
			assertSame(second, secondType.getClassLoader());
			assertEquals("first", firstType.getMethod("value").invoke(firstType.getConstructor().newInstance()));
			assertEquals("second", secondType.getMethod("value").invoke(secondType.getConstructor().newInstance()));
			assertThrows(ClassNotFoundException.class, () -> Class.forName("isolated.Probe"));
		}
		// In particular, no loader may keep a JAR locked on Windows after close().
		Files.delete(firstJar);
		Files.delete(secondJar);
	}

	@Test
	void scansSignaturesAndFieldsWithoutInitializingMinecraftClasses() throws Exception {
		Path jar = createJar("uninitialized", "public int left; public int right; "
				+ "static { if (Boolean.parseBoolean(\"true\")) throw new IllegalStateException(\"initialized\"); }");
		try (URLClassLoader loader = ClasspathUtil.createClassLoader(jar.toFile())) {
			String signature = ReflectionUtil.generateClassSignature("isolated.Probe", loader);
			assertTrue(signature.contains("fld:int+fld:int+"), signature);
			assertEquals("right", ReflectionUtil.getFieldWithType("isolated.Probe", "int", 2, loader));
			assertThrows(ExceptionInInitializerError.class, () -> Class.forName("isolated.Probe", true, loader));
		}
	}

	@Test
	void rejectsMissingJarsAndCleansUpAFailedProcessorBinding() throws Exception {
		Path missing = directory.resolve("missing.jar");
		assertThrows(java.io.IOException.class, () -> ClasspathUtil.createClassLoader(missing.toFile()));
		try (WorldProcessor processor = new WorldProcessor()) {
			processor.setJarLocation(missing.toFile());
			assertFalse(processor.bindToMinecraft());
			Path jar = createJar("unsupported", "public int value;");
			processor.setJarLocation(jar.toFile());
			assertFalse(processor.bindToMinecraft());
			assertFalse(processor.isBound());
			Files.delete(jar);
		}
	}

	private Path createJar(String name, String members) throws Exception {
		Path root = Files.createDirectory(directory.resolve(name));
		Path source = Files.createDirectories(root.resolve("isolated")).resolve("Probe.java");
		Files.writeString(source, "package isolated; public class Probe { " + members + " }");
		assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
				"-d", root.toString(), source.toString()));
		Path jar = root.resolve("fixture.jar");
		try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
			output.putNextEntry(new JarEntry("isolated/Probe.class"));
			Files.copy(root.resolve("isolated/Probe.class"), output);
			output.closeEntry();
		}
		return jar;
	}
}
