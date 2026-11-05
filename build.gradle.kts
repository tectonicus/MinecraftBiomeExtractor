import org.gradle.api.tasks.WriteProperties

plugins {
	`java-library`
	application
}

group = "biomeextractor"
version = "1.0"

repositories {
	mavenCentral()
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
	withSourcesJar()
}

application {
	mainClass.set("biomeextractor.MinecraftBiomeExtractor")
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
}

val generateBuildInfo = tasks.register<WriteProperties>("generateBuildInfo") {
	destinationFile.set(layout.buildDirectory.file("generated/build-info/biomeextractor.buildInfo"))
	property("version", project.version.toString())
	property("buildNumber", providers.environmentVariable("BUILD_NUMBER").orElse("development"))
}

tasks.processResources {
	from(generateBuildInfo)
}

tasks.named<Jar>("sourcesJar") {
	from(generateBuildInfo)
}

tasks.jar {
	manifest.attributes("Main-Class" to application.mainClass.get())
}

val prepareIntegrationTest = tasks.register<Sync>("prepareIntegrationTest") {
	from("testdata") {
		exclude("**/*.jar", "**/*.jar.new", "**/*.png")
	}
	into(layout.buildDirectory.dir("integration-test/testdata"))
}

testing.suites {
	withType<JvmTestSuite>().configureEach {
		useJUnitJupiter("6.1.3")
	}

	register<JvmTestSuite>("integrationTest") {
		dependencies {
			implementation(project())
		}
		targets.all {
			testTask.configure {
				dependsOn(prepareIntegrationTest)
				workingDir(layout.buildDirectory.dir("integration-test"))
				shouldRunAfter(tasks.test)
				listOf("MINECRAFT_BETA_13_JAR", "MINECRAFT_BETA_13_SERVER_JAR", "MINECRAFT_BETA_181_JAR",
						"MINECRAFT_1_0_JAR", "MINECRAFT_ALPHA_120_JAR").forEach { name ->
					val jar = providers.environmentVariable(name).filter { it.isNotBlank() }
					inputs.property(name, jar.orElse(""))
					if (jar.isPresent) inputs.file(jar).withPropertyName("${name}_contents")
				}
			}
		}
	}
}

tasks.withType<Test>().configureEach {
	systemProperty("java.awt.headless", "true")
}

// Build the historical tests by default; run extraction explicitly with integrationTest.
tasks.check {
	dependsOn("integrationTestClasses")
}
