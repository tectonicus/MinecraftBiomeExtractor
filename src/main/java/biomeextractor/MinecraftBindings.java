package biomeextractor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** A complete, validated set of bindings from one historical API generation. */
final class MinecraftBindings {
	final BindingProfile profile;
	final boolean server;
	private final Constructor<?> saveHandler;
	private final Constructor<?> world;
	private final Constructor<?> biomeGenerator;
	private final Field loadedFlag;
	private final Method temperatures;
	private final Method moistures;
	private final Method generate;
	private final Field temperatureField;
	private final Field moistureField;
	private Object generator;

	MinecraftBindings(BindingProfile profile, ClassLoader loader, Map<String, String> signatures)
			throws ReflectiveOperationException {
		this.profile = profile;
		Class<?> worldClass = match("world", loader, signatures);
		Class<?> generatorClass = match("biomeGenerator", loader, signatures);
		Constructor<?> worldConstructor;
		boolean serverConstructor;
		if (profile.worldApi == BindingProfile.WorldApi.DIRECT) {
			saveHandler = null;
			try {
				worldConstructor = worldClass.getConstructor(File.class, String.class);
			} catch (NoSuchMethodException e) {
				worldConstructor = worldClass.getConstructor(File.class, String.class, long.class);
			}
			serverConstructor = false;
		} else {
			Class<?> handlerType = match("saveHandlerInterface", loader, signatures);
			Class<?> handlerClass = match("saveHandler", loader, signatures);
			if (!handlerType.isInterface() || !handlerType.isAssignableFrom(handlerClass)) {
				throw new NoSuchMethodException("Save-handler classes do not implement the same interface");
			}
			saveHandler = handlerClass.getConstructor(File.class, String.class, boolean.class);
			Class<?> worldArgument = profile.worldApi == BindingProfile.WorldApi.SAVE_HANDLER
					? long.class : match("worldInfo", loader, signatures);
			try {
				worldConstructor = worldClass.getConstructor(handlerType, String.class, worldArgument);
				serverConstructor = false;
			} catch (NoSuchMethodException e) {
				Class<?> dimensionClass = match("serverDimension", loader, signatures);
				worldConstructor = worldClass.getConstructor(handlerType, String.class, worldArgument, dimensionClass);
				serverConstructor = true;
			}
		}
		world = worldConstructor;
		server = serverConstructor;
		biomeGenerator = generatorClass.getConstructor(worldClass);
		String loadedName = ReflectionUtil.getFieldWithType(worldClass.getName(), "boolean", 3, loader);
		loadedFlag = worldClass.getField(loadedName);
		if (profile.usesDoubleFields) {
			generate = generatorClass.getMethod("a", int.class, int.class, int.class, int.class);
			temperatureField = generatorClass.getField("a");
			moistureField = generatorClass.getField("b");
			if (temperatureField.getType() != double[].class || moistureField.getType() != double[].class) {
				throw new NoSuchFieldException("Expected double[] climate fields");
			}
			temperatures = null;
			moistures = null;
		} else {
			temperatures = generatorClass.getMethod("a", float[].class, int.class, int.class, int.class, int.class);
			moistures = generatorClass.getMethod("b", float[].class, int.class, int.class, int.class, int.class);
			if (temperatures.getReturnType() != float[].class || moistures.getReturnType() != float[].class) {
				throw new NoSuchMethodException("Expected float[] climate methods");
			}
			generate = null;
			temperatureField = null;
			moistureField = null;
		}
	}

	void loadWorld(File directory) throws ReflectiveOperationException {
		generator = null;
		Object minecraftWorld;
		if (profile.worldApi == BindingProfile.WorldApi.DIRECT) {
			minecraftWorld = world.getParameterCount() == 2 ? world.newInstance(directory, "")
					: world.newInstance(directory, "", 0L);
		} else {
			Object handler = saveHandler.newInstance(directory, "", false);
			Object argument = profile.worldApi == BindingProfile.WorldApi.SAVE_HANDLER ? Long.valueOf(0L) : null;
			minecraftWorld = server ? world.newInstance(handler, "", argument, null)
					: world.newInstance(handler, "", argument);
		}
		if (loadedFlag.getBoolean(minecraftWorld)) {
			throw new IllegalStateException("Minecraft could not load the world");
		}
		generator = biomeGenerator.newInstance(minecraftWorld);
	}

	float[] climate(boolean temperature, float[] destination, int x, int z, int width, int depth)
			throws ReflectiveOperationException {
		requireWorld();
		int size = size(width, depth);
		if (!profile.usesDoubleFields) {
			return (float[])(temperature ? temperatures : moistures).invoke(generator, destination, x, z, width, depth);
		}
		Climate climate = legacyClimate(x, z, width, depth);
		double[] values = temperature ? climate.temperatures : climate.moistures;
		if (destination == null || destination.length < size) {
			destination = new float[size];
		}
		for (int i = 0; i < size; i++) {
			destination[i] = (float)values[i];
		}
		return destination;
	}

	double climateAtBlock(boolean temperature, int x, int z) throws ReflectiveOperationException {
		requireWorld();
		if (!profile.usesDoubleFields) {
			return climate(temperature, null, x, z, 1, 1)[0];
		}
		generate.invoke(generator, x, z, 1, 1);
		return ((double[])(temperature ? temperatureField : moistureField).get(generator))[0];
	}

	ByteCoord[] coordinates(ByteCoord[] destination, float[] temps, float[] moists, int x, int z, int width, int depth)
			throws ReflectiveOperationException {
		requireWorld();
		int size = size(width, depth);
		if (destination == null || destination.length != size) {
			destination = new ByteCoord[size];
		}
		if (profile.usesDoubleFields) {
			Climate climate = legacyClimate(x, z, width, depth);
			for (int i = 0; i < size; i++) {
				setCoordinate(destination, i, climate.temperatures[i], climate.moistures[i] * climate.temperatures[i]);
			}
		} else {
			temps = climate(true, temps, x, z, width, depth);
			moists = climate(false, moists, x, z, width, depth);
			for (int i = 0; i < size; i++) {
				// Retain the original float multiplication for the newer generator API.
				setCoordinate(destination, i, temps[i], moists[i] * temps[i]);
			}
		}
		return destination;
	}

	private Climate legacyClimate(int x, int z, int width, int depth) throws ReflectiveOperationException {
		Climate result = new Climate(size(width, depth));
		// The legacy generator assumes square noise arrays and stores them in X-major order.
		// Small square tiles also bound the extra allocation for long, narrow requests.
		for (int fromZ = 0; fromZ < depth; fromZ += 16) {
			for (int fromX = 0; fromX < width; fromX += 16) {
				int tileWidth = Math.min(16, width - fromX);
				int tileDepth = Math.min(16, depth - fromZ);
				int side = Math.max(tileWidth, tileDepth);
				generate.invoke(generator, x + fromX, z + fromZ, side, side);
				double[] temps = (double[])temperatureField.get(generator);
				double[] moists = (double[])moistureField.get(generator);
				for (int tileZ = 0; tileZ < tileDepth; tileZ++) {
					for (int tileX = 0; tileX < tileWidth; tileX++) {
						int source = tileX * side + tileZ;
						int target = fromX + tileX + (fromZ + tileZ) * width;
						result.temperatures[target] = temps[source];
						result.moistures[target] = moists[source];
					}
				}
			}
		}
		return result;
	}

	private Class<?> match(String role, ClassLoader loader, Map<String, String> signatures) throws ClassNotFoundException {
		String reference = profile.signature(role);
		String best = null;
		int bestScore = 1;
		for (Map.Entry<String, String> candidate : signatures.entrySet()) {
			int score = score(reference, candidate.getValue());
			if (score > bestScore) {
				best = candidate.getKey();
				bestScore = score;
			}
		}
		if (best == null) {
			throw new ClassNotFoundException("No signature match for " + role);
		}
		return Class.forName(best, false, loader);
	}

	private static int score(String reference, String candidate) {
		Map<String, Integer> remaining = new HashMap<>();
		String[] parts = candidate.split("\\+");
		for (String part : parts) {
			String key = part.toLowerCase(Locale.ROOT);
			remaining.put(key, remaining.getOrDefault(key, 0) + 1);
		}
		int matches = 0;
		for (String part : reference.split("\\+")) {
			String key = part.toLowerCase(Locale.ROOT);
			int count = remaining.getOrDefault(key, 0);
			if (count > 0) {
				matches++;
				remaining.put(key, count - 1);
			}
		}
		return 2 * matches - parts.length;
	}

	private void requireWorld() {
		if (generator == null) {
			throw new IllegalStateException("Load a world before requesting biome data");
		}
	}

	private static int size(int width, int depth) {
		if (width <= 0 || depth <= 0) {
			throw new IllegalArgumentException("Biome area dimensions must be positive");
		}
		return Math.multiplyExact(width, depth);
	}

	private static void setCoordinate(ByteCoord[] destination, int index, double temperature, double rainfall) {
		if (destination[index] == null) {
			destination[index] = new ByteCoord();
		}
		destination[index].x = (byte)((1.0 - temperature) * 255.0);
		destination[index].y = (byte)((1.0 - rainfall) * 255.0);
	}

	private static final class Climate {
		final double[] temperatures;
		final double[] moistures;

		Climate(int size) {
			temperatures = new double[size];
			moistures = new double[size];
		}
	}
}
