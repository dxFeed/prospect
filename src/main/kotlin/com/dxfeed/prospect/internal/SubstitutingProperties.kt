/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.internal

import com.dxfeed.prospect.FlatName
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties

internal class SubstitutingProperties private constructor() : Properties() {

    private val result = mutableMapOf<String, String>()

    override fun put(key: Any, value: Any): Any? {
        val k = expand(key as? String, result)
        val v = expand(value as? String, result)
        return if (k == null || v == null) null else result.put(k, v)
    }

    companion object {

        fun readFromFile(file: String): Map<FlatName, String> {
            val actualFile = file.trim().takeIf { it.isNotEmpty() }
                ?.let { File(it) }?.takeIf { it.exists() }
                ?: return emptyMap()

            val props = SubstitutingProperties()
            try {
                FileInputStream(actualFile).use { props.load(it) }
            } catch (e: IOException) {
                throw RuntimeException("Unable to read properties from file: $actualFile", e)
            }

            return props.result
        }

        private fun expand(text: String?, properties: Map<String, String>): String? {
            var t = text ?: return null

            for (n in 0..999) {
                val i = t.indexOf("\${")
                val j = t.indexOf('}', i + 2)

                if (i < 0 || j < i + 2) return t

                val key = t.substring(i + 2, j)
                val value = getSystem(key) ?: properties[key]

                t = t.substring(0, i) + (value ?: "") + t.substring(j + 1)
            }

            return t
        }

        /**
         * Ignores all security exceptions.
         */
        private fun getSystem(key: String) = try {
            System.getProperty(key) ?: System.getenv()[key] ?: System.getenv(key)
        } catch (ignored: SecurityException) {
            null
        }
    }
}
