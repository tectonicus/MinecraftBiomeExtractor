import org.gradle.api.tasks.WriteProperties

plugins {
	`java-library`
	application
	`maven-publish`
	signing
}

group = "org.tectonicus"
version = "2.0.0"

repositories {
	mavenCentral()
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(21))
	withSourcesJar()
	withJavadocJar()
}

application {
	mainClass.set("org.tectonicus.biomeextractor.MinecraftBiomeExtractor")
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

tasks.withType<Jar>().configureEach {
	metaInf { from("LICENSE") }
}

// Fail the build on missing or broken Javadoc in the public API.
tasks.javadoc {
	(options as StandardJavadocDocletOptions).apply {
		addBooleanOption("Xdoclint:all", true)
		addBooleanOption("Werror", true)
		quiet()
	}
}

tasks.jar {
	manifest.attributes("Main-Class" to application.mainClass.get())
}

publishing {
	publications {
		register<MavenPublication>("maven") {
			from(components["java"])
			pom {
				name.set("Minecraft Biome Extractor")
				description.set("Extracts biome colormap coordinates from historical (pre-Anvil) Minecraft worlds.")
				url.set("https://github.com/tectonicus/MinecraftBiomeExtractor")
				licenses {
					license {
						name.set("MIT")
						url.set("https://opensource.org/license/mit")
					}
				}
				developers {
					developer {
						id.set("skoeven")
						name.set("Steven Koeven")
						url.set("https://github.com/skoeven")
					}
				}
				scm {
					url.set("https://github.com/tectonicus/MinecraftBiomeExtractor")
					connection.set("scm:git:https://github.com/tectonicus/MinecraftBiomeExtractor.git")
					developerConnection.set("scm:git:ssh://git@github.com/tectonicus/MinecraftBiomeExtractor.git")
				}
			}
		}
	}
}

// Central requires signed artifacts. Provide an ASCII-armored key through the signingInMemoryKey and
// signingInMemoryKeyPassword Gradle properties (or ORG_GRADLE_PROJECT_* env vars); otherwise publishing to Central
// uses the gpg CLI. Local publishes are signed only when a key is configured.
// findProperty also sees ORG_GRADLE_PROJECT_* environment variables, unlike providers.gradleProperty.
// Blank values count as missing, since GitHub Actions passes an unset secret as an empty string.
fun nonBlankProperty(name: String): String? = (findProperty(name) as String?)?.takeIf { it.isNotBlank() }

val signingKey = nonBlankProperty("signingInMemoryKey")
signing {
	if (signingKey != null) {
		// Gradle creates no signatory for a null password; an unprotected key uses an empty one.
		useInMemoryPgpKeys(signingKey, findProperty("signingInMemoryKeyPassword") as String? ?: "")
	}
	// Without a signatory, signing is skipped unless required; publishing to Central requires it (below).
	isRequired = false
	sign(publishing.publications)
}

fun isCentralPublish(task: Task) = task.name.contains("CentralPortal")

// Decided from the task graph rather than the command line, so abbreviated task names are covered too.
gradle.taskGraph.whenReady {
	if (allTasks.any(::isCentralPublish)) {
		signing.isRequired = true
		if (signingKey == null) signing.useGpgCmd()
	}
}

// Publishing a release runs the checks first, and with -PreleaseTag verifies that the tag matches the version.
val releaseTag = providers.gradleProperty("releaseTag")
val projectVersion = version.toString()
val verifyReleaseVersion = tasks.register("verifyReleaseVersion") {
	description = "Checks that the -PreleaseTag value, minus a leading 'v', matches the project version."
	inputs.property("releaseTag", releaseTag.orElse(""))
	doLast {
		val tag = releaseTag.orNull ?: return@doLast
		check(tag.removePrefix("v") == projectVersion) {
			"Release tag '$tag' does not match project version '$projectVersion'. Update version in build.gradle.kts."
		}
	}
}

tasks.matching(::isCentralPublish).configureEach {
	dependsOn(verifyReleaseVersion, tasks.check)
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
