package com.dxfeed.gradle.autover

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


open class PrintVersionTask : DefaultTask() {

    init {
        group = TASK_GROUP_NAME
        description = "Print version of the project"
    }

    @TaskAction
    fun run() {
        val verFile = project.file(VERSION_FILE)
        val curVer = readVersion(verFile)
        logger.quiet(curVer)
    }

}
