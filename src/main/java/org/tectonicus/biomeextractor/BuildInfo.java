package org.tectonicus.biomeextractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Reads the version details the build writes to {@code biomeextractor.buildInfo}. */
final class BuildInfo {
	static final String RESOURCE = "/biomeextractor.buildInfo";
	private static final String DEVELOPMENT_BUILD = "development";

	private static final Properties PROPERTIES = load();

	private BuildInfo() {}

	private static Properties load() {
		Properties properties = new Properties();
		try (InputStream input = BuildInfo.class.getResourceAsStream(RESOURCE)) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException e) {
			// Fall back to "unknown" rather than stopping the application over a version label.
		}
		return properties;
	}

	/** Returns the version, or {@code unknown} when the build info is missing. */
	static String version() {
		return PROPERTIES.getProperty("version", "unknown");
	}

	/** Returns the version, with the CI build number when there is one, for example {@code 2.0.0 (build 42)}. */
	static String displayVersion() {
		String build = PROPERTIES.getProperty("buildNumber", "").trim();
		return build.isEmpty() || DEVELOPMENT_BUILD.equals(build) ? version() : version() + " (build " + build + ")";
	}
}
