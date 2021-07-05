/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.internal

import com.dxfeed.prospect.FlatProps
import com.dxfeed.prospect.Props
import com.dxfeed.prospect.appendNestedName

internal class NestedProp<Nested : Props>(
    override val name: String,
    internal val instance: Nested
) : PropNode {

    override fun init(
        parentPrefix: String,
        flatProps: FlatProps,
        initReport: InitReport
    ) {
        val nodePrefix = parentPrefix.appendNestedName(name)
        instance.loadImpl(nodePrefix, flatProps, initReport)
    }

    override fun collectLeaves(collect: (PropLeaf) -> Unit) {
        instance.collectLeaves(collect)
    }

    override fun toString(): String {
        return buildString {
            append(name)
            append(" = ")
            append(instance.toString())
        }
    }
}
