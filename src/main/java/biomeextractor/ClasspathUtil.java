package biomeextractor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ClasspathUtil {
	private ClasspathUtil() {
	}

	/** Creates an independent Minecraft loader. The caller must close it after use. */
	public static URLClassLoader createClassLoader(File jarFile) throws IOException {
		if (jarFile == null || !jarFile.isFile()) {
			throw new IOException("Minecraft JAR does not exist: " + jarFile);
		}
		return new URLClassLoader(new URL[] {jarFile.toURI().toURL()}, ClasspathUtil.class.getClassLoader());
	}
}
