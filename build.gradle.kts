@file:Suppress("UnstableApiUsage", "RedundantNullableReturnType")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.ajoberstar.grgit.Grgit
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.2.1"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
}

val grgit: Grgit? = project.grgit

var canPublish = grgit != null && System.getenv("RELEASE") != null

fun getVersionSuffix(): String {
	return grgit?.branch?.current()?.name ?: "nogit+${properties["minecraft_version"]}"
}

group = properties["maven_group"]!!

if (System.getenv().containsKey("NEW_TAG")) {
	version = System.getenv("NEW_TAG").substring(1)
} else {
	val versionStr = "${properties["mod_version"]}+${properties["minecraft_version"]!!}"
	canPublish = false
	version = if (grgit != null) {
		"$versionStr+dev-${grgit.log()[0].abbreviatedId}"
	} else {
		"$versionStr+dev-nogit"
	}
}

val isBundlingSearchables = properties["bundle_searchables"] == "true"

base {
	archivesName.set("${properties["archives_base_name"]}")
}

repositories {
	// Parchment Mappings
	maven {
		name = "ParchmentMC"
		url = uri("https://maven.parchmentmc.org")
		content {
			includeGroup("org.parchmentmc.data")
		}
	}

	// Mod Menu, EMI
	maven {
		name = "TerraformersMC"
		url = uri("https://maven.terraformersmc.com/releases/")
		content {
			includeGroup("com.terraformersmc")
			includeGroup("dev.emi")
		}
	}

	maven {
		name = "Nucleoid"
		url = uri("https://maven.nucleoid.xyz/")
		content {
			includeGroup("eu.pb4")
		}
	}

	// YACL
	maven {
		name = "Xander Maven"
		url = uri("https://maven.isxander.dev/releases")
		content {
			includeGroupAndSubgroups("dev.isxander")
			includeGroupAndSubgroups("org.quiltmc")
		}
	}

	// YACL Snapshots
	maven {
		name = "Xander Snapshot Maven"
		url = uri("https://maven.isxander.dev/snapshots")
		content {
			includeGroupAndSubgroups("dev.isxander")
			includeGroupAndSubgroups("org.quiltmc")
		}
	}

	// Searchables
	maven {
		name = "BlameJared"
		url = uri("https://maven.blamejared.com")
		content {
			includeGroupAndSubgroups("com.blamejared.searchables")
		}
	}

	// Dev Utils, Jade
	maven {
		name = "Modrinth Maven"
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}

	// Where Is It, JackFredLib
	maven {
		name = "JackFredMaven"
		url = uri("https://maven.jackf.red/releases/")
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

java {
	withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

	mods {
		create("chesttracker") {
			sourceSet(sourceSets["client"])
		}
	}

	log4jConfigs.from(file("log4j2.xml"))

	runConfigs.configureEach {
		this.programArgs.addAll("--username JackFred".split(" "))
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

	// Where is it
	modImplementation("red.jackf:whereisit:${properties["where-is-it_version"]}")
	include("red.jackf:whereisit:${properties["where-is-it_version"]}")

	// Config
	modImplementation("dev.isxander:yet-another-config-lib:${properties["yacl_version"]}") {
		exclude(group = "com.terraformersmc", module = "modmenu")
	}

	// dev util
	//modLocalRuntime("dev.emi:emi-fabric:${properties["emi_version"]}")
	//modLocalRuntime("maven.modrinth:jsst:mc1.20-0.3.12")

	////////////////
	// MOD COMPAT //
	////////////////

	// Searchables
	modCompileOnly("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}")
	modLocalRuntime("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}")
	if (isBundlingSearchables) include("com.blamejared.searchables:Searchables-fabric-${properties["searchables_version"]}")

	// Mod Menu
	modCompileOnly("com.terraformersmc:modmenu:${properties["modmenu_version"]}")
	modLocalRuntime("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

	// Shulker Box Tooltip
	modCompileOnly("com.misterpemodder:shulkerboxtooltip-fabric:${properties["shulkerboxtooltip_version"]}")

	//modLocalRuntime("com.misterpemodder:shulkerboxtooltip-fabric:${properties["shulkerboxtooltip_version"]}")
	//modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:${properties["clothconfig_version"]}")

	// WTHIT
	modCompileOnly("mcp.mobius.waila:wthit-api:${properties["wthit_version"]}")

	//modLocalRuntime("mcp.mobius.waila:wthit:${properties["wthit_version"]}")
	//modLocalRuntime("lol.bai:badpackets:${properties["badpackets_version"]}")

	// Jade
	modCompileOnly("maven.modrinth:jade:${properties["jade_version"]}")

	modLocalRuntime("maven.modrinth:jade:${properties["jade_version"]}")

	// Litematica
	modCompileOnly("maven.modrinth:litematica:${properties["litematica_version"]}")
	modCompileOnly("maven.modrinth:malilib:${properties["malilib_version"]}")

	//modLocalRuntime("maven.modrinth:litematica:${properties["litematica_version"]}")
	//modLocalRuntime("maven.modrinth:malilib:${properties["malilib_version"]}")
}

tasks.withType<ProcessResources>().configureEach {
	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(17)
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${properties["archivesBaseName"]}"}
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"]!!)
		}
	}

	repositories {
		// if not in CI we publish to maven local
		if (!System.getenv().containsKey("CI")) repositories.mavenLocal()

		if (canPublish) {
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
}

if (canPublish) {
	val lastTag = if (System.getenv("PREVIOUS_TAG") == "NONE") null else System.getenv("PREVIOUS_TAG")
	val newTag = "v$version"

	var generateChangelogTask: TaskProvider<GenerateChangelogTask>? = null

	// Changelog Generation
	if (lastTag != null) {
		val changelogHeader = if (properties.containsKey("changelogHeaderAddon")) {
			val addonProp: String = properties["changelogHeaderAddon"]!!.toString()

			if (addonProp.isNotBlank()) {
				addonProp
			} else {
				null
			}
		} else {
			null
		}

		val changelogFileText = rootProject.file("changelogs/${properties["mod_version"]}.md")
			.takeIf { it.exists() }
			?.readText()

		generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
			this.lastTag.set(lastTag)
			this.newTag.set(newTag)
			githubUrl.set(properties["github_url"]!!.toString())
			prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))

			val bundledText = if (isBundlingSearchables) {
                """
                |Bundled:
                |  - Where Is It: ${properties["where-is-it_version"]}
                |  - Searchables: ${properties["searchables_version"]}
                |  """.trimMargin()
            } else {
                """
                |Bundled:
                |  - Where Is It: ${properties["where-is-it_version"]}
                |  """.trimMargin()
            }

			// Add a bundled block for each module version
			prologue.set(listOfNotNull(changelogHeader, changelogFileText, bundledText).joinToString(separator = "\n\n"))
		}
	}

	val changelogTextProvider = if (generateChangelogTask != null) {
		provider {
			generateChangelogTask!!.get().changelogFile.get().asFile.readText()
		}
	} else {
		provider {
			"No Changelog Generated"
		}
	}

	// GitHub Release
	tasks.named<GithubReleaseTask>("githubRelease") {
		generateChangelogTask?.let { dependsOn(it) }

		authorization = System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" }
		owner = properties["github_owner"]!!.toString()
		repo = properties["github_repo"]!!.toString()
		tagName = newTag
		releaseName = "${properties["mod_name"]} $newTag"
		targetCommitish = grgit!!.branch.current().name
		releaseAssets.from(
			tasks["remapJar"].outputs.files,
			tasks["remapSourcesJar"].outputs.files,
		)
		subprojects.forEach {
			releaseAssets.from(
				it.tasks["remapJar"].outputs.files,
				it.tasks["remapSourcesJar"].outputs.files,
			)
		}

		body = changelogTextProvider
	}

	// Mod Platforms
	if (listOf("CURSEFORGE_TOKEN", "MODRINTH_TOKEN").any { System.getenv().containsKey(it) }) {
		publishMods {
			changelog.set(changelogTextProvider)
			type.set(when(properties["release_type"]) {
				"release" -> ReleaseType.STABLE
				"beta" -> ReleaseType.BETA
				else -> ReleaseType.ALPHA
			})
			modLoaders.add("fabric")
			modLoaders.add("quilt")
			file.set(tasks.named<RemapJarTask>("remapJar").get().archiveFile)

			if (System.getenv().containsKey("CURSEFORGE_TOKEN") || dryRun.get()) {
				curseforge {
					projectId.set("397217")
					accessToken.set(System.getenv("CURSEFORGE_TOKEN"))
					properties["game_versions_curse"]!!.toString().split(",").forEach {
						minecraftVersions.add(it)
					}
					displayName.set("${properties["prefix"]!!} ${properties["mod_name"]!!} ${version.get()}")
					listOf("fabric-api", "yacl").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("where-is-it").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "roughly-enough-items", "modmenu", "shulkerboxtooltip", "wthit", "jade").forEach {
						optional {
							slug.set(it)
						}
					}

					if (isBundlingSearchables) {
						embeds {
							slug.set("searchables")
						}
					} else {
						optional {
							slug.set("searchables")
						}
					}
				}
			}

			if (System.getenv().containsKey("MODRINTH_TOKEN") || dryRun.get()) {
				modrinth {
					accessToken.set(System.getenv("MODRINTH_TOKEN"))
					projectId.set("ni4SrKmq")
					properties["game_versions_mr"]!!.toString().split(",").forEach {
						minecraftVersions.add(it)
					}
					displayName.set("${properties["mod_name"]!!} ${version.get()}")
					listOf("fabric-api", "yacl").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("where-is-it").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "rei", "modmenu", "shulkerboxtooltip", "wthit", "jade").forEach {
						optional {
							slug.set(it)
						}
					}

					if (isBundlingSearchables) {
						embeds {
							slug.set("searchables")
						}
					} else {
						optional {
							slug.set("searchables")
						}
					}
				}
			}
		}
	}
}

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
	mcVersion.set(properties["minecraft_version"]!!.toString())
	loader.set("fabric")
}