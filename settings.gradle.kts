plugins {
	id("com.gradleup.nmcp.settings") version "1.6.2"
}

rootProject.name = "biome-extractor"

// Central Portal token: set mavenCentralUsername/mavenCentralPassword in ~/.gradle/gradle.properties,
// or MAVEN_CENTRAL_USERNAME/MAVEN_CENTRAL_PASSWORD in the environment. ORG_GRADLE_PROJECT_* variables
// are project properties and are not visible here.
// Values are resolved here because nmcp isolates these settings for each project. Blank values count as missing,
// since GitHub Actions passes an unset secret as an empty string.
fun credential(property: String, env: String): String? =
	providers.gradleProperty(property).filter { it.isNotBlank() }
		.orElse(providers.environmentVariable(env).filter { it.isNotBlank() }).orNull

nmcpSettings {
	centralPortal {
		username = credential("mavenCentralUsername", "MAVEN_CENTRAL_USERNAME")
		password = credential("mavenCentralPassword", "MAVEN_CENTRAL_PASSWORD")
		// Release automatically once Central validates; publishing a GitHub release is the manual gate.
		publishingType = "AUTOMATIC"
	}
}
