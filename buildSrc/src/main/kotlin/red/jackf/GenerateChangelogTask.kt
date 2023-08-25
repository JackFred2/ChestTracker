package red.jackf

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream

/**
 * Generates a markdown changelog from a list of commits between two tags.
 *
 * Adapted from TerraformersMC's ferry script
 */
abstract class GenerateChangelogTask : DefaultTask() {
    /**
     * Previous tag to start grabbing commits from
     */
    @get:Input
    abstract val lastTag: Property<String>

    /**
     * Current tag to grab commits until
     */
    @get:Input
    abstract val newTag: Property<String>

    /**
     * List of commit filters. Commit titles are checked if they are prefixed with any of these, and if so are added to
     * the changelog. To disable the filter, add an empty string ("").
     */
    @get:Input
    val prefixFilters: ListProperty<String> = project.objects.listProperty(String::class.java).also {
        it.addAll("[feat]", "[fix]", "[docs]")
    }

    /**
     * GitHub URL to add to the changelog. If present, a comparison URL will be added to the changelog header
     */
    @get:Input @get:Optional
    abstract val githubUrl: Property<String>

    /**
     * String to prepend to the changelog. Is inserted between the "Previous:" lines and the changelog list / github comparison
     * URL.
     */
    @get:Input @get:Optional
    abstract val prologue: Property<String>

    /**
     * String to append to the changelog. Is inserted after the changelog list.
     */
    @get:Input @get:Optional
    abstract val epilogue: Property<String>

    /**
     * Output path of the changelog file.
     */
    @get:OutputFile
    val changelogFile: RegularFileProperty = project.objects.fileProperty().also { it.convention(project.provider {
        project.layout.buildDirectory.file("changelogs/${lastTag.get()}..${newTag.get()}.md").get()
    })}

    @TaskAction
    fun generateChangelog() {
        println("Writing changelog to ${changelogFile.get()}")

        val command = listOf("git","log","--max-count=100","--pretty=format:\"%s\"","${lastTag.get()}...${newTag.get()}")

        val lines = mutableListOf(
            // "# ${properties["mod_name"]} $newTag",
            "Previous: ${lastTag.get()}",
            ""
        )

        if (prologue.isPresent) {
            lines.add(prologue.get())
            lines.add("")
        }

        if (githubUrl.isPresent) {
            lines.add("Full changelog: ${githubUrl.get()}/compare/${lastTag.get()}...${newTag.get()}")
            lines.add("")
        }

        val stream = ByteArrayOutputStream()
        project.exec {
            it.commandLine = command
            it.standardOutput = stream
        }
        stream.toString().lines().forEach { line ->
            var str = line
            // it starts with quotes in github actions i guess https://www.youtube.com/watch?v=-O3ogWBfWI0
            if (str.startsWith("\"")) str = str.substring(1)
            if (str.endsWith("\"")) str = str.substring(0, str.length - 1)
            if (prefixFilters.get().any { prefix -> str.startsWith(prefix) })
                lines.add("  - $str")
        }

        if (epilogue.isPresent) {
            lines.add("")
            lines.add(epilogue.get())
        }

        val changelog = lines.joinToString("\n")
        changelogFile.get().asFile.writeText(changelog)
    }
}