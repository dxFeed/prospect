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
