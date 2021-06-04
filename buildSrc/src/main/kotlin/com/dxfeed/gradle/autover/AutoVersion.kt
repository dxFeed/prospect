package com.dxfeed.gradle.autover

import org.apache.commons.configuration2.PropertiesConfiguration
import org.gradle.api.GradleException
import java.io.File


const val VERSION_FILE = "version.properties"
const val VERSION_PROP = "version"
const val DEFAULT_VERSION = "0.0.1-SNAPSHOT"
const val TASK_GROUP_NAME = "version"

const val SET_VERSION_TASK = "setVersion"

fun writeVersion(versionFile: File, newVersion: String) {
    val versionProps = PropertiesConfiguration()
    versionProps.header = """
            THIS FILE IS AUTO-GENERATED.
            Use `$SET_VERSION_TASK` task to change the version.
        """.trimIndent()
    versionProps.setProperty(VERSION_PROP, newVersion)
    versionFile.writer().use { versionProps.write(it) }
}

fun readVersion(versionFile: File): String {
    if (!versionFile.exists()) {
        throw GradleException("Version file does not exist: $VERSION_FILE. Use `$SET_VERSION_TASK` task to create it.")
    }

    val versionProps = PropertiesConfiguration()
    versionFile.reader().use { versionProps.read(it) }

    return versionProps.getString(VERSION_PROP, null)?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: throw GradleException("Property $VERSION_PROP is missing in $VERSION_FILE")
}

