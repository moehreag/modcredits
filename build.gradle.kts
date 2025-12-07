@file:Suppress("UnstableApiUsage")

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.14.+"
    id("maven-publish")
    id("io.freefair.lombok") version "9.0.+"
    id("com.modrinth.minotaur") version "2.+"
}

val modVersion = "1.0.0"
group = "io.github.moehreag"
val loader = "0.18.1"
val minecraft = "1.21.11"
val fabric = "0.139.4+1.21.11"
val parchment = "2025.10.12"
val modmenu = "17.0.0-alpha.1"
version = "$modVersion+$minecraft"

base {
    archivesName = "modcredits"
}

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.terraformersmc.com")
}

loom {
    mods {
        create("moehreag-modcredits") {
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
        parchment("org.parchmentmc.data:parchment-1.21.10:${parchment}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${loader}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric}")

    modCompileOnly("com.terraformersmc:modmenu:$modmenu")
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
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
        maven {
            name = "owlMaven"
            val repository =
                if (project.version.toString().contains("beta") || project.version.toString().contains("alpha")
                ) "snapshots" else "releases"
            url = uri("https://moehreag.duckdns.org/maven/$repository")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "pR1Fpbbv"
    versionNumber = "$version"
    versionType = "release"
    uploadFile = tasks.remapJar.get()
    gameVersions.set(listOf(minecraft))
    loaders.set(listOf("fabric", "quilt"))
    additionalFiles.set(listOf(tasks.remapSourcesJar))
    syncBodyFrom = file("README.md").readText()
    dependencies {

    }
}