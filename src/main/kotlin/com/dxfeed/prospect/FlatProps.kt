/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

/**
 * Full name that identifies a property in [FlatProps].
 */
public typealias FlatName = String

/**
 * Human-readable description of a source from which property values originated.
 */
public typealias PropSource = String

/**
 * Raw value of a property, usually obtained after reading properties from a file.
 */
public data class FlatValue(
    public val value: String,
    public val source: PropSource? = null
)

/**
 * A mapping of flat names to flat values.
 */
public interface FlatProps {

    public val keys: Set<FlatName>

    public fun getValue(key: FlatName): FlatValue?
}

/**
 * Alias for [FlatProps.getValue].
 */
public operator fun FlatProps.get(key: FlatName): FlatValue? = getValue(key)

/**
 * Flat props backed by a map.
 */
public class MapFlatProps(
    map: Map<FlatName, FlatValue>
) : FlatProps {

    private val map: Map<FlatName, FlatValue> = map.toMap()
    override val keys: Set<FlatName> get() = map.keys
    override fun getValue(key: FlatName): FlatValue? = map[key]

    public fun merge(other: FlatProps): MapFlatProps {
        val resultMap = mutableMapOf<FlatName, FlatValue>()
        resultMap += map

        for (key in other.keys) {
            val otherValue = other[key] ?: continue
            resultMap[key] = otherValue
        }

        return MapFlatProps(resultMap)
    }
}

/**
 * Creates [MapFlatProps] from this map.
 */
public fun Map<FlatName, FlatValue>.toFlatProps(): MapFlatProps = MapFlatProps(this)

/**
 * Converts this map to flat props, [trimming][trim] keys and values if required.
 *
 * Each flat value is assigned a given [source].
 */
public fun Map<FlatName, String>.toFlatProps(
    source: String? = null,
    trim: Boolean = true
): MapFlatProps {

    val s = if (source != null && trim) source.trim() else source
    val m = mutableMapOf<FlatName, FlatValue>()
    for ((k, v) in this) {
        if (trim) {
            m[k.trim()] = FlatValue(v.trim(), s)
        } else {
            m[k] = FlatValue(v, s)
        }
    }

    return m.toFlatProps()
}
