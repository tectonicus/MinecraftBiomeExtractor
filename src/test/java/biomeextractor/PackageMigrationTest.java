package biomeextractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageMigrationTest {
	@Test
	void resolvesClassesInTheRenamedPackageForReflection() {
		String signature = ReflectionUtil.generateClassSignature(ByteCoord.class.getName());
		assertTrue(signature.contains("carg:byte carg:byte"), signature);
		assertEquals("x", ReflectionUtil.getFieldWithType(ByteCoord.class.getName(), "byte", 1));
		assertEquals("y", ReflectionUtil.getFieldWithType(ByteCoord.class.getName(), "byte", 2));
	}
}
