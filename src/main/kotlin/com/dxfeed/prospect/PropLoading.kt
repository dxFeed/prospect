/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import com.dxfeed.prospect.internal.SubstitutingProperties

/**
 * Load property values from a key-value pairs.
 */
public fun <P : Props> P.load(
    vararg properties: Pair<FlatName, String>,
    options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
    source: PropSource? = null,
    trim: Boolean = true,
): P = run {
    load(mapOf(*properties).toFlatProps(source, trim), options)
}

/**
 * Load property values from a properties file.
 */
public fun <P : Props> P.loadFromFile(
    propertiesFile: String,
    options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
): P = run {
    val flatProps = SubstitutingProperties.readFromFile(propertiesFile)
        .toFlatProps("file $propertiesFile")
    load(flatProps, options)
}

/**
 * Load property values from properties file, overriding them from another file.
 */
public fun <P : Props> P.loadOverridableFromFile(
    propertiesFile: String,
    propertiesOverrideFile: String? = "$propertiesFile.override",
    options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
): P = run {
    var flatProps: MapFlatProps = SubstitutingProperties.readFromFile(propertiesFile)
        .toFlatProps("file $propertiesFile")
    if (propertiesOverrideFile != null) {
        val overrideProps = SubstitutingProperties.readFromFile(propertiesOverrideFile)
            .toFlatProps("file $propertiesOverrideFile")
        flatProps = flatProps.merge(overrideProps)
    }

    load(flatProps, options)
}

/**
 * Load property values from properties file specified in a system property,
 * overriding them from another file, specified in another system property.
 *
 * If [propertiesFileSysProp] is not set, then the value of this
 * parameter itself is used as a file name. The same applies to the
 * [propertiesOverrideFileSysProp].
 */
public fun <P : Props> P.loadOverridableFromFileInSysProp(
    propertiesFileSysProp: String,
    propertiesOverrideFileSysProp: String? = "$propertiesFileSysProp.override",
    options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
): P = run {
    val templateFile = propertiesFileSysProp.let { System.getProperty(it, it) }
    val overrideFile = propertiesOverrideFileSysProp?.let { System.getProperty(it, it) }
    loadOverridableFromFile(templateFile, overrideFile, options)
}

public fun <P : Props> P.load(
    flatProps: FlatProps,
    options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
): P = apply {
    tryLoad(flatProps, options)
}

internal fun buildCompoundErrorMessage(errors: List<PropError>): String {
    require(errors.isNotEmpty())

    val errorsBySource = errors.groupBy { it.source }
        .mapValues { (_, value) -> value.sortedBy { it.flatName } }
        .toSortedMap(compareBy { it })

    return buildString {
        append("Found ${errors.size} errors:")
        for ((source, sourceErrors) in errorsBySource) {
            if (sourceErrors.isEmpty()) continue
            appendLine()
            if (source == null) {
                append("<unknown source>")
            } else {
                append(source)
            }
            append(":")

            for (sourceError in sourceErrors) {
                appendLine()
                append("\t")
                append(sourceError.flatName)
                append(": ")
                append(sourceError.message)
                sourceError.cause?.let {
                    append("; cause: ${it.stackTraceToString()}".replace("\n", "\n\t"))
                }
            }
        }
    }
}
