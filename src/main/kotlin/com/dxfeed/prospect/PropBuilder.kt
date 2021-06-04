package com.dxfeed.prospect

import com.dxfeed.prospect.internal.Prop
import com.dxfeed.prospect.internal.PropChecker
import com.dxfeed.prospect.internal.PropDefaulter
import com.dxfeed.prospect.internal.PropFormatter
import com.dxfeed.prospect.internal.PropParser
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public class PropBuilder<V : Any> internal constructor(
    private var name: String? = null,
) {

    private var parser: PropParser<V>? = null
    private var defaultProvider: PropDefaulter<V>? = null
    private var useDefaultOnEmpty: Boolean = false
    private var formatter: PropFormatter<V> = { it.toString() }
    private val checkers = mutableListOf<PropChecker<V>>()

    public operator fun <P : Props> provideDelegate(thisRef: P, property: KProperty<*>): ReadWriteProperty<P, V> {
        return build<P>(name ?: property.name).also { thisRef.register(it) }
    }

    /**
     * Sets [parser] for the value.
     *
     * Calling this method replaces a parser set previously.
     *
     * The contract for the parser is the following.
     * - The parser is called during property loading with [flat value][FlatValue.value] as its argument.
     * - The parser is called only if the flat value is a non-empty string.
     * - If the parser returns null, the loading continues as if the flat value initially was an _empty_ string.
     */
    public fun parse(parser: PropParser<V>): PropBuilder<V> = apply {
        this.parser = parser
    }

    /**
     * Sets a default value that is evaluated when the key is missing during loading.
     *
     * When [useOnEmpty] is true, empty flat values are processed as if they were missing from the flat props.
     */
    public fun withDefault(useOnEmpty: Boolean = false, defaultValue: () -> V): PropBuilder<V> = apply {
        this.useDefaultOnEmpty = useOnEmpty
        this.defaultProvider = defaultValue
    }

    /**
     * Adds a checker with an optional [message] to verify parsed value.
     *
     * Note that checks are *accumulated* in contrast to other configurations.
     */
    public fun check(message: ((V) -> String)? = null, check: (V) -> Boolean): PropBuilder<V> = apply {
        this.checkers += PropChecker(message, check)
    }

    /**
     * Sets custom formatter for value.
     */
    public fun format(formatter: (V) -> String): PropBuilder<V> = apply {
        this.formatter = formatter
    }

    private fun <P : Props> build(kPropertyName: String): Prop<P, V> {
        val name = this.name ?: kPropertyName
        return Prop(
            name,
            isOptional = false,
            ensureParser(name),
            defaultProvider,
            useDefaultOnEmpty,
            formatter,
            checkers.toList()
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <P : Props> buildOptional(kPropertyName: String): Prop<P, V?> {
        val name = this.name ?: kPropertyName
        return Prop(
            name = name,
            isOptional = true,
            parser = ensureParser(name),
            defaultProvider = defaultProvider as PropDefaulter<V?>?,
            useDefaultOnEmpty = useDefaultOnEmpty,
            formatter = formatter as PropFormatter<V?>,
            checkers = checkers.toList() as List<PropChecker<V?>>,
        )
    }

    private fun ensureParser(name: String): PropParser<V> = parser ?: error("parser is not provided (name: $name)")
}

public class OptionalPropBuilder<P : Props, V : Any>(
    private val propBuilder: PropBuilder<V>,
) {

    public operator fun provideDelegate(thisRef: P, property: KProperty<*>): ReadWriteProperty<P, V?> {
        return propBuilder.buildOptional<P>(property.name).also { thisRef.register(it) }
    }
}

/**
 * Sets a default value that is used when the key is missing during loading.
 *
 * When [useOnEmpty] is true, empty flat values are processed as if they were missing from the flat props.
 */
public fun <V : Any> PropBuilder<V>.withDefault(defaultValue: V, useOnEmpty: Boolean = false): PropBuilder<V> {
    return withDefault(useOnEmpty) { defaultValue }
}

/**
 * Adds a checker with a [message] to verify parsed value.
 *
 * Note, that checks are accumulated in contrast to other configurations.
 */
public fun <V : Any> PropBuilder<V>.check(message: String, check: (V) -> Boolean): PropBuilder<V> =
    this.check({ message }, check)

/**
 * Makes this property optional.
 *
 * Optional properties have value of null, when their key is missing during loading.
 */
public fun <P : Props, V : Any> PropBuilder<V>.optional(): OptionalPropBuilder<P, V> {
    return OptionalPropBuilder(this)
}
