/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
