/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import com.dxfeed.prospect.internal.InitReport
import com.dxfeed.prospect.internal.PropLeaf
import com.dxfeed.prospect.internal.PropNode

/**
 * Provides context define a group of properties in a declarative manner.
 *
 * Actual properties are declared in a child class.
 */
public abstract class Props {

    private var parent: Props? = null
    private val props = ArrayList<PropNode>()

    private val isRoot get() = parent === null

    /**
     * Creates a delegate for the `by`-expression.
     *
     * @see PropBuilder
     */
    public fun <V : Any> property(name: String? = null): PropBuilder<V> {
        return PropBuilder(name)
    }

    public fun <Nested : Props> nested(
        name: String? = null,
        create: () -> Nested,
    ): NestedPropBuilder<Nested> {

        return createNestedPropertyBuilder(name, create)
    }

    public fun <Included : Props> included(
        create: () -> Included,
    ): NestedPropBuilder<Included> {

        return createNestedPropertyBuilder(name = "", create)
    }

    internal fun tryLoad(
        flatProps: FlatProps,
        options: PropLoadingOptions = PropLoadingOptions.DEFAULT,
    ) {
        check(isRoot) { "Nested properties must be initialised via the root" }

        val initReport = InitReport(flatProps.keys, ignoreMissing = options.ignoreMissing)
        loadImpl(parentPrefix = "", flatProps = flatProps, initReport = initReport)

        val errors = initReport.errors.toMutableList()

        if (!options.ignoreUnknown && initReport.unknown.isNotEmpty()) {
            for (unknownProp in initReport.unknown) {
                errors += PropError(unknownProp, flatProps[unknownProp]?.source, "unknown key")
            }
        }

        if (errors.isNotEmpty()) {
            throw InvalidPropsException(errors)
        }
    }

    public fun toMultilineString(): String {
        val leaves = collectLeaves().filter { it.hasFlatName }
        return leaves.joinToString("\n") { "${it.flatName} = ${it.formatValue()}" }
    }

    override fun toString(): String = toMultilineString()

    private fun <Nested : Props> createNestedPropertyBuilder(
        name: String? = null,
        create: () -> Nested,
    ): NestedPropBuilder<Nested> {

        val instance = createNestedInstance(create)
        return NestedPropBuilder(instance, name)
    }

    internal fun register(propNode: PropNode) {
        props += propNode
    }

    private fun <Nested : Props> createNestedInstance(create: () -> Nested): Nested {
        val instance = create()

        check(instance.isRoot) {
            "Properties have already been nested elsewhere"
        }

        return instance
    }

    internal fun loadImpl(
        parentPrefix: String,
        flatProps: FlatProps,
        initReport: InitReport,
    ) {
        props.forEach { prop ->
            prop.init(parentPrefix, flatProps, initReport)
        }
    }

    internal fun collectLeaves(collect: (PropLeaf) -> Unit) {
        for (prop in props) {
            prop.collectLeaves(collect)
        }
    }

    private fun collectLeaves(): List<PropLeaf> {
        val leaves = ArrayList<PropLeaf>()
        collectLeaves { leaves.add(it) }
        return leaves
    }

    public companion object
}
