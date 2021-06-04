/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.ext

import com.dxfeed.prospect.PropBuilder

public fun PropBuilder<List<String>>.parseList(
    separator: String = ",",
    trim: Boolean = true
): PropBuilder<List<String>> = apply {
    return parse { input ->
        parseListImpl(input, trim, separator, ArrayList()) { it }
    }
}

public fun <T> PropBuilder<List<T>>.parseList(
    separator: String = ",",
    trim: Boolean = true,
    transform: (String) -> T
): PropBuilder<List<T>> = apply {
    return parse { input ->
        parseListImpl(input, trim, separator, ArrayList(), transform)
    }
}

public fun <C : Collection<String>> PropBuilder<C>.parseListTo(
    destination: (List<String>) -> C,
    separator: String = ",",
    trim: Boolean = true
): PropBuilder<C> = apply {
    return parse { input ->
        val items = parseListImpl(input, trim, separator, ArrayList()) { it }
        destination(items)
    }
}

public fun <T, C : Collection<T>> PropBuilder<C>.parseListTo(
    destination: (List<T>) -> C,
    separator: String = ",",
    trim: Boolean = true,
    transform: (String) -> T
): PropBuilder<C> = apply {
    return parse { input ->
        val items = parseListImpl(input, trim, separator, ArrayList(), transform)
        destination(items)
    }
}

private fun <V, C : MutableCollection<in V>> parseListImpl(
    input: String,
    trim: Boolean,
    separator: String,
    into: C,
    transform: (String) -> V
): C {
    return if (input.isEmpty()) {
        into
    } else {
        val trimmed = (if (trim) input.trim() else input)
        trimmed.split(separator).mapTo(into) {
            transform(if (trim) it.trim() else it)
        }
    }
}
