package biomeextractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

enum BindingProfile {
	BETA_1_8_1("Beta 1.8.1", "beta-1.8.1.properties", WorldApi.WORLD_INFO, false),
	BETA_1_3_01("Beta 1.3_01", "beta-1.3_01.properties", WorldApi.SAVE_HANDLER, true),
	ALPHA_1_2_0_02("Alpha v1.2.0_02", "alpha-1.2.0_02.properties", WorldApi.DIRECT, true);

	enum WorldApi {
		DIRECT, SAVE_HANDLER, WORLD_INFO
	}

	final String displayName;
	final WorldApi worldApi;
	final boolean usesDoubleFields;
	private final Properties signatures = new Properties();

	BindingProfile(String displayName, String resource, WorldApi worldApi, boolean usesDoubleFields) {
		this.displayName = displayName;
		this.worldApi = worldApi;
		this.usesDoubleFields = usesDoubleFields;
		try (InputStream input = BindingProfile.class.getResourceAsStream("/biomeextractor/signatures/" + resource)) {
			if (input == null) {
				throw new IOException("Missing binding profile: " + resource);
			}
			signatures.load(input);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	String signature(String role) {
		String signature = signatures.getProperty(role);
		if (signature == null) {
			throw new IllegalArgumentException("Missing signature for " + role + " in " + displayName);
		}
		return signature;
	}
}
