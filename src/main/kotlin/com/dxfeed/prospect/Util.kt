/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

private const val NAME_SEP = "."

internal fun String.appendNestedName(name: String): String {
    return when {
        this.isEmpty() -> name
        name.isEmpty() -> this
        else -> "$this$NAME_SEP$name"
    }
}
