package com.dxfeed.gradle.autover

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.property


open class SetVersionTask : DefaultTask() {

    init {
        group = TASK_GROUP_NAME
        description = "Changes version of this project and all subprojects"
    }

    @Input
    val newVersion = project.objects.property<String>()

    @Option(
        option = "newVersion",
        description = "Value for the new version"
    )
    fun setNewVersion(value: String) {
        newVersion.set(value)
    }

    @TaskAction
    fun run() {
        val verFile = project.file(VERSION_FILE)
        val v = newVersion.orNull
            ?: throw GradleException(
                """
                    `newVersion` option is not provided.
                    Pass it as command-line argument:
                    $ ./gradlew $SET_VERSION_TASK --newVersion=1.0.0
                """.trimIndent()
            )

        writeVersion(verFile, v)

        logger.lifecycle("Set version to $v")
    }

}
