plugins {
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("maven-publish")
	id("io.freefair.lombok") version "8.12"
}

version = "1.0.0"
group = "com.example"
val loader = "0.16.10"
val minecraft = "1.21.4"
val fabric = "0.117.0+1.21.4"
val parchment = "2025.02.16"
val modmenu = ""

base {
	archivesName = "modid"
}

repositories {
	maven("https://maven.parchmentmc.org")
	maven("https://maven.terraformersmc.com")
}

loom {
	mods {
		create("modid") {
			sourceSet("main")
		}
	}

}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${minecraft}")
	mappings(loom.layered {
		officialMojangMappings {
			nameSyntheticMembers = true
		}
		parchment("org.parchmentmc.data:parchment-${minecraft}:${parchment}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${loader}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric}")

	modImplementation("com.terraformersmc:modmenu:13.0.2")
}

tasks.processResources {
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType(JavaCompile::class).configureEach {
	options.release = 21
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.getByName("jar", Jar::class) {
	filesMatching("LICENSE") {
		rename("^(LICENSE.*?)(\\..*)?$", "\$1_${base.archivesName}\$2")
	}
}

// configure the maven publication
publishing {
	publications {
		create("mavenJava", MavenPublication::class) {
			artifactId = base.archivesName.get()
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}