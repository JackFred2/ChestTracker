@file:Suppress("UnstableApiUsage")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask
import java.net.URI

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.4-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.0.+"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
	// id("io.github.juuxel.loom-vineflower") version "1.11.0"
}

group = properties["maven_group"]!!
version = "${properties["mod_version"]!!}+${properties["minecraft_version"]!!}"

val modReleaseType = when(properties["type"]) {
	"alpha" -> ReleaseType.ALPHA
	"beta" -> ReleaseType.BETA
	else -> ReleaseType.STABLE
}

base {
	archivesName.set("${properties["archives_base_name"]}")
}

repositories {
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

	maven {
		name = "QuiltMC"
		url = uri("https://maven.quiltmc.org/repository/release")
		content {
			includeGroupAndSubgroups("org.quiltmc")
		}
	}

	// Searchables
	maven {
		name = "BlameJared"
		url = URI("https://maven.blamejared.com")
		content {
			includeGroupAndSubgroups("com.blamejared.searchables")
		}
	}

	// Dev Utils, Jade
	maven {
		name = "Modrinth Maven"
		url = URI("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	// Where Is It, JackFredLib
	maven {
		name = "JackFredMaven"
		url = URI("https://maven.jackf.red/releases/")
		content {
			includeGroupAndSubgroups("red.jackf")
		}
	}

	// Shulker Box Tooltip
	maven {
		name = "MisterPeModder"
		url = uri("https://maven.misterpemodder.com/libs-release/")
		content {
			includeGroupAndSubgroups("com.misterpemodder")
		}
	}

	// Cloth Config
	maven {
		name = "Shedaniel"
		url = uri("https://maven.shedaniel.me")
		content {
			includeGroupAndSubgroups("me.shedaniel")
		}
	}

	// WTHIT
	maven {
		url  = uri("https://maven2.bai.lol")
		content {
			includeGroupAndSubgroups("lol.bai")
			includeGroupAndSubgroups("mcp.mobius.waila")
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

	buildscript.sourceFile
		?.parentFile
		?.resolve("log4j2.xml")
		?.let { log4jConfigs.from(it) }

	runConfigs.configureEach {
		this.programArgs.addAll("--username JackFred".split(" "))
		this.vmArgs.add("-XX:+AllowEnhancedClassRedefinition")
	}


	accessWidenerPath.set(file("src/client/resources/chesttracker.accesswidener"))
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric-api_version"]}")
	modCompileOnly("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

	modImplementation("red.jackf:whereisit:${properties["where-is-it_version"]}") {
		exclude(group = "com.terraformersmc", module = "modmenu")
	}
	include("red.jackf:whereisit:${properties["where-is-it_version"]}") {
		exclude(group = "com.terraformersmc", module = "modmenu")
	}

	// Config
	modImplementation("dev.isxander.yacl:yet-another-config-lib-fabric:${properties["yacl_version"]}") {
		exclude(group = "com.terraformersmc", module = "modmenu")
	}
	implementation("blue.endless:jankson:${properties["jankson_version"]}")

	// Gui
	modImplementation("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}")
	include("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}")

	// dev util
	modCompileOnly("dev.emi:emi-fabric:${properties["emi_version"]}:api")
	//modLocalRuntime("dev.emi:emi-fabric:${properties["emi_version"]}")
	//modLocalRuntime("maven.modrinth:jsst:mc1.20-0.3.12")

	// mod compat
	modImplementation("com.misterpemodder:shulkerboxtooltip-fabric:${properties["shulkerboxtooltip_version"]}")

	modCompileOnly("mcp.mobius.waila:wthit-api:${properties["wthit_version"]}")

	modLocalRuntime("mcp.mobius.waila:wthit:${properties["wthit_version"]}")
	modLocalRuntime("lol.bai:badpackets:${properties["badpackets_version"]}")

	// modCompileOnly("maven.modrinth:jade:${properties["jade_version"]}")
	// modLocalRuntime("maven.modrinth:jade:${properties["jade_version"]}")
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
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

val lastTagVal = properties["lastTag"]?.toString()
val newTagVal = properties["newTag"]?.toString()
if (lastTagVal != null && newTagVal != null) {
	val generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
		lastTag.set(lastTagVal)
		newTag.set(newTagVal)
		githubUrl.set(properties["github_url"]!!.toString())
		prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))
	}

	if (System.getenv().containsKey("GITHUB_TOKEN")) {
		tasks.named<GithubReleaseTask>("githubRelease") {
			dependsOn(generateChangelogTask)

			authorization.set(System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" })
			owner.set(properties["github_owner"]!!.toString())
			repo.set(properties["github_repo"]!!.toString())
			tagName.set(newTagVal)
			releaseName.set("${properties["mod_name"]} $newTagVal")
			targetCommitish.set(grgit.branch.current().name)
			releaseAssets.from(
				tasks["remapJar"].outputs.files,
				tasks["remapSourcesJar"].outputs.files,
			)

			body.set(provider {
				return@provider generateChangelogTask.get().changelogFile.get().asFile.readText()
			})
		}
	}

	tasks.named<DefaultTask>("publishMods") {
		dependsOn(generateChangelogTask)
	}

	if (listOf("CURSEFORGE_TOKEN", "MODRINTH_TOKEN").any { System.getenv().containsKey(it) }) {
		publishMods {
			changelog.set(provider {
				return@provider generateChangelogTask.get().changelogFile.get().asFile.readText()
			})
			type.set(modReleaseType)
			modLoaders.add("fabric")
			modLoaders.add("quilt")
			file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)
			// additionalFiles.from(tasks.named<RemapSourcesJarTask>("remapSourcesJar").get().archiveFile)

			if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
				curseforge {
					projectId.set("397217")
					accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
					properties["game_versions"]!!.toString().split(",").forEach {
						minecraftVersions.add(it)
					}
					displayName.set("${properties["prefix"]!!} ${properties["mod_name"]!!} ${version.get()}")
					listOf("fabric-api", "yacl", "where-is-it").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("searchables").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "roughly-enough-items", "modmenu", "shulkerboxtooltip", "wthit").forEach {
						optional {
							slug.set(it)
						}
					}
				}
			}

			if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
				modrinth {
					accessToken.set(System.getenv("MODRINTH_TOKEN"))
					projectId.set("ni4SrKmq")
					properties["game_versions"]!!.toString().split(",").forEach {
						minecraftVersions.add(it)
					}
					displayName.set("${properties["mod_name"]!!} ${version.get()}")
					listOf("fabric-api", "yacl", "where-is-it").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("searchables").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "rei", "modmenu", "shulkerboxtooltip", "wthit").forEach {
						optional {
							slug.set(it)
						}
					}
				}
			}
		}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "JackFredMaven"
			url = uri("https://maven.jackf.red/releases/")
			content {
				includeGroupByRegex("red.jackf.*")
			}
			credentials {
				username = properties["jfmaven.user"]?.toString() ?: System.getenv("JACKFRED_MAVEN_USER")
				password = properties["jfmaven.key"]?.toString() ?: System.getenv("JACKFRED_MAVEN_PASS")
			}
		}
	}
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
	mcVersion.set(properties["minecraft_version"]!!.toString())
	loader.set("fabric")
}