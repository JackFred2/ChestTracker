@file:Suppress("UnstableApiUsage")

import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options
import red.jackf.UpdateDependenciesTask
import java.net.URI

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.2-SNAPSHOT"
	id("com.modrinth.minotaur") version "2.+"
	id("com.matthewprenger.cursegradle") version "1.4.0"
}

fun Project.findPropertyStr(name: String) = findProperty(name) as String?

group = findProperty("maven_group") !!
version = findPropertyStr("mod_version") ?: "dev"

val modReleaseType = findPropertyStr("type") ?: "release"

base {
	archivesName.set("${findProperty("archives_base_name")}-${findProperty("minecraft_version")}")
}

repositories {
	mavenLocal {
		content {
			includeGroup("red.jackf")
		}
	}

	// Parchment Mappings
	maven {
		name = "ParchmentMC"
		url = URI("https://maven.parchmentmc.org")
		content {
			includeGroup("org.parchmentmc.data")
		}
	}

	// Mod Menu, EMI
	maven {
		name = "TerraformersMC"
		url = URI("https://maven.terraformersmc.com/releases/")
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	// YACL
	maven {
		name = "Xander Maven"
		url = URI("https://maven.isxander.dev/releases")
		content {
			includeGroup("dev.isxander.yacl")
		}
	}

	// YACL Dependencies
	maven {
		name = "Sonatype"
		url = URI("https://oss.sonatype.org/content/repositories/snapshots")
		content {
			includeGroupByRegex("com.twelvemonkeys.*")
		}
	}

	// Searchables
	maven {
		name = "BlameJared"
		url = URI("https://maven.blamejared.com")
		content {
			includeGroupByRegex("com.blamejared.searchables.*")
		}
	}

	// Dev Utils
	maven {
		name = "Modrinth Maven"
		url = URI("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	// Where Is It, JackFredLib
	maven {
		name = "GitHubPackages"
		url = URI("https://maven.pkg.github.com/JackFred2/WhereIsIt")
		content {
			includeGroup("red.jackf")
		}
		credentials {
			username = findPropertyStr("gpr.user")
			password = findPropertyStr("gpr.key")
		}
	}
}

loom {
    splitEnvironmentSourceSets()

	mods {
		create("chesttracker") {
			sourceSet(sourceSets["client"])
		}
	}

	runConfigs {
		configureEach {
			val path = buildscript.sourceFile?.parentFile?.resolve("log4j2.xml")
			path?.let { property("log4j2.configurationFile", path.path) }
		}
	}

	accessWidenerPath.set(file("src/client/resources/chesttracker.accesswidener"))
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${findProperty("minecraft_version")}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${findProperty("parchment_version")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${findProperty("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${findProperty("fabric-api_version")}")
	modImplementation("com.terraformersmc:modmenu:${findProperty("modmenu_version")}")

	modImplementation("red.jackf:whereisit:${findProperty("where-is-it_version")}")

	// Config
	modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:${findProperty("yacl_version")}")
	implementation("blue.endless:jankson:${findProperty("jankson_version")}")

	// Gui
	modImplementation("com.blamejared.searchables:Searchables-fabric-1.20.1:${findProperty("searchables_version")}")
	include("com.blamejared.searchables:Searchables-fabric-1.20.1:${findProperty("searchables_version")}")

	// dev util
	modCompileOnly("dev.emi:emi-fabric:${findProperty("emi_version")}:api")
	modLocalRuntime("dev.emi:emi-fabric:${findProperty("emi_version")}")
	//modLocalRuntime("maven.modrinth:jsst:mc1.20-0.3.12")
}

tasks.withType<ProcessResources>().configureEach {
	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${findProperty("archivesBaseName")}"}
	}
}

curseforge {
	if (System.getenv("CURSEFORGE_TOKEN") != null && version != "UNKNOWN") {
		apiKey = System.getenv("CURSEFORGE_TOKEN")
		project(closureOf<CurseProject> {
			id = "397217"
			changelog = "Check the GitHub for changes: https://github.com/JackFred2/ChestTracker/releases"
			releaseType = "release"

			releaseType = modReleaseType

			addGameVersion("Fabric")
			addGameVersion("Quilt")
			addGameVersion("Java 17")

			project.findPropertyStr("game_versions")?.split(",")?.forEach { addGameVersion(it) }

			mainArtifact(tasks.remapJar.get().archiveFile, closureOf<CurseArtifact> {
				relations(closureOf<CurseRelation> {
					requiredDependency("fabric-api")
					requiredDependency("yacl")
					optionalDependency("modmenu")
				})
				displayName = if (project.hasProperty("prefix")) {
					"${findPropertyStr("prefix")} ${base.archivesName.get()}-$version.jar"
				} else {
					"${base.archivesName.get()}-$version.jar"
				}
			})

		})

		options(closureOf<Options> {
			forgeGradleIntegration = false
		})
	} else {
		println("No CURSEFORGE_TOKEN set, skipping...")
	}
}

modrinth {
	if (System.getenv("MODRINTH_TOKEN") != null && version != "UNKNOWN") {
		token.set(System.getenv("MODRINTH_TOKEN"))
		projectId.set("ni4SrKmq")
		versionNumber.set(version as String)
		versionName.set("Chest Tracker $version")
		versionType.set(modReleaseType)
		uploadFile.set(tasks.remapJar)
		changelog.set("Check the GitHub for changes: https://github.com/JackFred2/ChestTracker/releases")
		project.findPropertyStr("game_versions")?.let {
			gameVersions.set(it.split(","))
		}
		loaders.set(listOf("fabric", "quilt"))
		dependencies {
			required.project("1eAoo2KR") // YACL
			required.project("P7dR8mSH") // fabric api

			optional.project("mOgUt4GM") // Mod Menu
		}
	} else {
		println("No MODRINTH_TOKEN set, skipping...")
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			setVersion(rootProject.version)
			groupId = group as String
			from(components["java"])
		}
	}

	repositories {
	}
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
	mcVersion.set(properties["minecraft_version"]!!.toString())
	loader.set("fabric")
}