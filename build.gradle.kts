@file:Suppress("UnstableApiUsage")

plugins {
    id("net.fabricmc.fabric-loom") version "1.17+"
    id("maven-publish")
    id("io.freefair.lombok") version "9.2.+"
    id("com.modrinth.minotaur") version "2.+"
}

val modVersion = "2.1.0"
group = "io.github.moehreag"
val loader = "0.19.3"
val minecraft = "26.2-rc-2"
val fabric = "0.151.0+26.2"
val modmenu = "18.0.0-alpha.8"
version = "$modVersion+$minecraft"

base {
    archivesName = "modcredits"
}

repositories {
    maven("https://maven.axolotlclient.com/releases")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.terraformersmc.com")
}

loom {
    mods {
        create("moehreag-modcredits") {
            sourceSet("main")
        }
    }
    runs {
        getByName("client") {
            jvmArguments.addAll("-XX:+AllowEnhancedClassRedefinition", "-XX:+IgnoreUnrecognizedVMOptions")
        }
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${minecraft}")
    implementation("net.fabricmc:fabric-loader:${loader}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    implementation("net.fabricmc.fabric-api:fabric-api:${fabric}")

    compileOnly("com.terraformersmc:modmenu:$modmenu")

    implementation(include("io.github.axolotlclient.AxolotlClient-config:AxolotlClientConfig-common:3.1.14")!!)
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.release = 25
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
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
            url = uri("https://maven.axolotlclient.com/$repository")
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
    uploadFile = tasks.jar.get()
    gameVersions.set(listOf(minecraft))
    loaders.set(listOf("fabric", "quilt"))
    additionalFiles.set(listOf(tasks.getByName("sourcesJar")))
    syncBodyFrom = file("README.md").readText()
    dependencies {

    }
}