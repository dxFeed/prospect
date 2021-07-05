/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

public class PropError(
    public val flatName: FlatName,
    public val source: PropSource? = null,
    public val message: String,
    public val cause: Throwable? = null,
) {
    override fun toString(): String = buildString {
        append("PropError[").append(flatName).append("]")
        append("(message=").append(message)
        append(", source=").append(source)
        append(", cause=").append(cause?.stackTraceToString())
        append(")")
    }
}
