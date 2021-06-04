package com.dxfeed.gradle.autover

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.property


open class BumpVersionTask : DefaultTask() {

    init {
        group = TASK_GROUP_NAME
        description = "Bumps version of the project"
    }

    @Input
    val appendSnapshot = project.objects.property<Boolean>().apply { set(true) }

    @Option(
        option = "appendSnapshot",
        description = "Whether '-SNAPSHOT' should be appended after bump"
    )
    fun setAppendSnapshot(value: String) {
        appendSnapshot.set(value.toBoolean())
    }

    @TaskAction
    fun run() {
        val verFile = project.file(VERSION_FILE)
        val curVer = readVersion(verFile)
        val curVerWithoutSuffix = curVer.removeSuffix("-SNAPSHOT")

        var newVer: String = incLastNumberInVersion(curVerWithoutSuffix)

        if (appendSnapshot.get()) {
            newVer += "-SNAPSHOT"
        }

        writeVersion(verFile, newVer)

        logger.info("Bumped version to $newVer")
        logger.quiet(newVer)
    }

    companion object {

        private val DOT_DELIMITED_VERSION_RE = "(\\d+(?:\\.\\d+)*).*".toRegex()

        fun incLastNumberInVersion(s: String): String {
            val matchResult = DOT_DELIMITED_VERSION_RE.matchEntire(s)
                ?: throw InvalidUserDataException(
                    "Version format is invalid." +
                            " Version neither starts with a number" +
                            " nor has a dot-prefixed number: $s"
                )

            val group = matchResult.groups[1]
                ?: error("match group is missing")

            val dotDelimitedVersion = group.value
            val versionNumbers = dotDelimitedVersion.split(".").map { it.toInt() }
            check(versionNumbers.isNotEmpty())

            val newDotDelimitedVersion = versionNumbers.mapIndexed { index, value ->
                if (index == versionNumbers.lastIndex) value + 1 else value
            }.joinToString(".") {
                it.toString()
            }

            return s.replaceRange(group.range, newDotDelimitedVersion)
        }

    }

}
