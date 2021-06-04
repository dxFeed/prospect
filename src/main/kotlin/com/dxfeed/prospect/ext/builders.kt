/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.ext

import com.dxfeed.prospect.PropBuilder
import com.dxfeed.prospect.Props
import com.dxfeed.prospect.internal.ParsingFailedException
import com.dxfeed.prospect.internal.PropParser
import com.dxfeed.prospect.withDefault

/**
 * Declares a `Boolean` non-nullable property with optional [default].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfBoolean(
    name: String? = null,
    default: Boolean? = null,
): PropBuilder<Boolean> = property(name, default, parser = ::parseBoolean)

/**
 * Declares an `Int` non-nullable property with optional [default].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfInt(
    name: String? = null,
    default: Int? = null,
): PropBuilder<Int> = property(name, default, parser = ::parseInt)

/**
 * Declares a `Long` non-nullable property with optional [default].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfLong(
    name: String? = null,
    default: Long? = null,
): PropBuilder<Long> = property(name, default, ::parseLong)

/**
 * Declares a `Double` non-nullable property with optional [default].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfDouble(
    name: String? = null,
    default: Double? = null,
): PropBuilder<Double> = property(name, default, ::parseDouble)

/**
 * Declares a `String` non-nullable property with optional [default].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfString(
    name: String? = null,
    default: String? = null,
): PropBuilder<String> = property(name, default, { it })

/**
 * Declares a URL non-nullable property,
 * which is backed up by `String` and will mask credentials when printed.
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfUrl(
    name: String? = null,
): PropBuilder<String> = propertyOfString(name).formatAsUrl()

/**
 * Declares a password non-nullable property,
 * which is backed up by `String` and will mask the value when printed.
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
public fun Props.propertyOfPassword(
    name: String? = null,
): PropBuilder<String> = propertyOfString(name).formatAsPassword()

/**
 * Declares a property with a [parser], optionally setting a [name] and
 * [default] value.
 */
public fun <V : Any> Props.property(
    name: String? = null,
    default: V? = null,
    parser: PropParser<V>,
): PropBuilder<V> {

    val p = property<V>(name).parse(parser)
    default?.let { p.withDefault(it) }
    return p
}

/**
 * Declares a list of strings non-nullable property with [default],
 * parsing input as CSV with given [separator].
 *
 * If [name] is not provided, name of a Kotlin property using
 * `by`-expression will be used.
 */
@JvmName("propertyOfListOfString")
public fun Props.propertyOfList(
    name: String? = null,
    default: List<String> = emptyList(),
    separator: String = ",",
    trim: Boolean = true,
): PropBuilder<List<String>> {

    return property<List<String>>(name)
        .withDefault(default, useOnEmpty = true)
        .parseList(separator, trim)
}

/**
 * Declares a list non-nullable property with [default],
 * parsing input as CSV with given [separator].
 */
@JvmName("propertyOfListOfItem")
public fun <Item> Props.propertyOfList(
    name: String? = null,
    default: List<Item> = emptyList(),
    separator: String = ",",
    trim: Boolean = true,
    transform: (String) -> Item,
): PropBuilder<List<Item>> {

    return property<List<Item>>(name)
        .withDefault(default, useOnEmpty = true)
        .parseList(separator, trim, transform)
}

private fun parseBoolean(value: String): Boolean {
    return value.toBoolean()
}

private fun parseInt(value: String): Int {
    return value.toIntOrNull() ?: throw ParsingFailedException()
}

private fun parseLong(value: String): Long {
    return value.toLongOrNull() ?: throw ParsingFailedException()
}

private fun parseDouble(value: String): Double {
    return value.toDoubleOrNull() ?: throw ParsingFailedException()
}
