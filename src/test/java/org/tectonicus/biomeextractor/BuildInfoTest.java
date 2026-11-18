package org.tectonicus.biomeextractor;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BuildInfoTest {
	@Test
	void readsVersionGeneratedByBuild() {
		assertNotNull(BuildInfoTest.class.getResource(BuildInfo.RESOURCE), "build info resource is on the classpath");
		assertNotEquals("unknown", BuildInfo.version());
		assertTrue(BuildInfo.version().matches("\\d+\\.\\d+\\.\\d+.*"), BuildInfo.version());
		assertTrue(BuildInfo.displayVersion().startsWith(BuildInfo.version()));
	}
}
