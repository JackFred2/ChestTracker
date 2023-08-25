@file:Suppress("UnstableApiUsage")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask
import java.net.URI

plugins {
	id("maven-publish")
	id("fabric-loom") version "1.3-SNAPSHOT"
	id("com.github.breadmoirai.github-release") version "2.4.1"
	id("org.ajoberstar.grgit") version "5.0.+"
	id("me.modmuss50.mod-publish-plugin") version "0.3.3"
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
	include("red.jackf:whereisit:${findProperty("where-is-it_version")}")

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
			type.set(ReleaseType.STABLE)
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
					listOf("fabric-api", "yacl").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("where-is-it", "searchables").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "roughly-enough-items", "modmenu").forEach {
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
					listOf("fabric-api", "yacl").forEach {
						requires {
							slug.set(it)
						}
					}
					listOf("where-is-it", "searchables").forEach {
						embeds {
							slug.set(it)
						}
					}
					listOf("emi", "jei", "rei", "modmenu").forEach {
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