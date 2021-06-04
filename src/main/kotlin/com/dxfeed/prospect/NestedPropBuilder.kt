package com.dxfeed.prospect

import com.dxfeed.prospect.internal.NestedProp
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public class NestedPropBuilder<Nested : Props> internal constructor(
    private val instance: Nested,
    private var name: String? = null
) {

    public operator fun <O : Props> provideDelegate(thisRef: O, property: KProperty<*>): ReadOnlyProperty<O, Nested> {
        if (this.name == null) {
            this.name = property.name
        }

        val prop = build()
        thisRef.register(prop)

        return ReadOnlyProperty { _, _ -> prop.instance }
    }

    private fun build(): NestedProp<Nested> {
        val name = name ?: error("Use by-expression")

        return NestedProp(
            name = name,
            instance = instance
        )
    }
}
