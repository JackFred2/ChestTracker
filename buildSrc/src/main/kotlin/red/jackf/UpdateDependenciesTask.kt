package red.jackf

import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL

/**
 * Tries to update mod dependencies by querying latest versions from Modrinth. Use properties with a <code>version</code>
 * suffix and place what you want updated between two comments: "# JF_AUTO_UPDATE_BLOCK" and "# JF_END_AUTO_UPDATE_BLOCK".
 * Properties should be in the format "<slug>-version="
 */
abstract class UpdateDependenciesTask : DefaultTask() {
    @get:Input
    val blockStartPrefix: Property<String> = project.objects.property(String::class.java).also { it.convention("# JF_AUTO_UPDATE_BLOCK") }

    @get:Input
    val blockEndPrefix: Property<String> = project.objects.property(String::class.java).also { it.convention("# JF_END_AUTO_UPDATE_BLOCK") }

    @get:InputFiles
    val filesToCheck: ConfigurableFileCollection = project.files("gradle.properties")

    @get:Input
    val modrinthEndpoint: Property<String> = project.objects.property(String::class.java).also { it.convention("https://api.modrinth.com/v2/") }

    @get:Input
    val stripLoaderSuffix: Property<Boolean> = project.objects.property(Boolean::class.java).also { it.convention(true) }

    @get:Input
    abstract val mcVersion: Property<String>

    @get:Input
    abstract val loader: Property<String>

    private fun getRequest(url: URL) : String {
        return with (url.openConnection()) {
            this.addRequestProperty("User-Agent", "JackFred2/BuildScript (via Groovy)")

            val response = StringBuffer()

            this.inputStream.bufferedReader().use { reader ->
                reader.lines().forEach {
                    response.append(it)
                }
            }

            response.toString()
        }
    }

    private fun getLatestVersion(slug: String) : String? {
        val url = "${modrinthEndpoint.get()}project/$slug/version?loaders=[\"${loader.get()}\"]&game_versions=[\"${mcVersion.get()}\"]".replace("\"", "%22")
        val response = getRequest(URL(url))
        val json = Json.parseToJsonElement(response).jsonArray
        if (json.size == 0) return null
        return json[0].jsonObject["version_number"]?.jsonPrimitive?.content
    }

    private fun stripLoaderSuffix(versionStr: String): String {
        val regex = Regex("^(?<actualVersion>.+).${loader.get()}$")
        val match = regex.matchEntire(versionStr) ?: return versionStr
        return match.groups["actualVersion"]!!.value
    }

    private val versionRegex = Regex("(?<slug>[\\w!@\$()`.+,\"\\-']{3,64})_version=(?<existing>.+)")

    private fun doFile(file: File) {
        var currentlyChecking = false
        val output = mutableListOf<String>()
        var updateCount = 0
        file.forEachLine { line ->
            when {
                line.contains(blockEndPrefix.get()) -> {
                    output.add(line)
                    currentlyChecking = false
                }
                line.contains(blockStartPrefix.get()) -> {
                    output.add(line)
                    currentlyChecking = true
                }
                !currentlyChecking -> {
                    output.add(line)
                }
                else -> {
                    val match = versionRegex.matchEntire(line)
                    if (match == null) {
                        output.add(line)
                    } else {
                        val slug = match.groups["slug"]!!.value
                        print("Checking MR for $slug.....")
                        val version = getLatestVersion(slug)
                        if (version == null) {
                            println("nothing found")
                            output.add(line)
                        } else {
                            print("found $version")
                            val processed = if (stripLoaderSuffix.get()) stripLoaderSuffix(version) else version
                            if (match.groups["existing"]!!.value != processed) {
                                println(".....updating")
                                output.add(line.replaceFirst(match.groups["existing"]!!.value, processed))
                                updateCount++
                            } else {
                                println()
                                output.add(line)
                            }
                        }
                    }
                }
            }
        }

        println("Updated $updateCount versions")

        file.writeText(output.joinToString("\n"))
    }

    @TaskAction
    fun pullLatestVersions() {
        for (file in filesToCheck) {
            if (file.exists() && file.isFile) {
                if (file.canWrite() && file.canRead()) {
                    doFile(file)
                } else {
                    println("[version updater] Insufficient permissions: $file")
                }
            } else {
                println("[version updater] Not a file: $file")
            }
        }
    }
}