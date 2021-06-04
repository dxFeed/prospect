package com.dxfeed.gradle.autover

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class AutoVersionPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {

        val versionFile = file(VERSION_FILE)

        if (!versionFile.exists()) {
            writeVersion(versionFile, DEFAULT_VERSION)
            logger.warn(
                """
                    Created $VERSION_FILE with default version: $DEFAULT_VERSION
                    Use `$SET_VERSION_TASK` task to change the version.
                """.trimIndent()
            )
        }

        val autoVersion = readVersion(versionFile)
        project.version = autoVersion
        subprojects {
            version = autoVersion
        }

        logger.info("Set version of this project and all subprojects to $autoVersion")

        tasks {
            register<PrintVersionTask>("printVersion")
            register<SetVersionTask>(SET_VERSION_TASK)
            register<BumpVersionTask>("bumpVersion")
        }
    }
}
