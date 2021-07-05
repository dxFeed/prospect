/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.internal

import com.dxfeed.prospect.FlatProps
import com.dxfeed.prospect.InvalidPropsException
import com.dxfeed.prospect.PropError
import com.dxfeed.prospect.PropSource
import com.dxfeed.prospect.Props
import com.dxfeed.prospect.appendNestedName
import com.dxfeed.prospect.get
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class Prop<P : Props, V>(
    override val name: String,
    private val isOptional: Boolean,
    private val parser: PropParser<V>,
    private val defaultProvider: PropDefaulter<V>?,
    private val useDefaultOnEmpty: Boolean,
    private val formatter: PropFormatter<V>,
    private val checkers: List<PropChecker<V>>,
) : PropLeaf, ReadWriteProperty<P, V> {

    // `null` means explicit absence of value; otherwise -- actual value
    private var propValue: V? = null

    private var _flatName: String? = null

    private val isSet: Boolean get() = propValue != null

    override operator fun getValue(thisRef: P, property: KProperty<*>): V {
        ensureFlatName(thisRef)
        return getInitializedValueOrThrow()
    }

    override fun setValue(thisRef: P, property: KProperty<*>, value: V) {
        ensureFlatName(thisRef)
        setNullableValueChecking(value)
    }

    override val hasFlatName: Boolean get() = _flatName != null

    override fun init(
        parentPrefix: String,
        flatProps: FlatProps,
        initReport: InitReport,
    ) {
        val flatName = parentPrefix.appendNestedName(name)

        this._flatName = flatName
        initReport.markSeen(flatName)

        val flatValue = flatProps[flatName]

        if (flatValue == null) {
            onInitAbsent(initReport)
            return
        }

        val source = flatValue.source
        val flatValueStr = flatValue.value

        if (flatValueStr.isEmpty()) {
            onInitEmpty(initReport, source)
            return
        }

        when (val parseResult = parseValue(flatValueStr)) {
            is ParseResult.Value -> {
                setNullableValueChecking(parseResult.value, initReport, source)
            }
            is ParseResult.Empty -> {
                onInitEmpty(initReport, source)
            }
            is ParseResult.Fail -> {
                val msg = "unable to parse value from '$flatValueStr'"
                val cause = parseResult.cause.takeIf { it !is ParsingFailedException }
                val err = PropError(flatName, source, msg, cause)
                initReport.addError(err)
            }
        }
    }

    private fun onInitEmpty(initReport: InitReport, source: PropSource?) {
        if (useDefaultOnEmpty) {
            onInitAbsent(initReport, source)
        } else {
            onInitUnset(initReport, source)
        }
    }

    private fun onInitAbsent(initReport: InitReport, source: PropSource? = null) {
        tryLoadingDefault(initReport)
        if (isOptional) return
        if (!isSet && !initReport.ignoreMissing) {
            reportMissingValue(initReport, source)
        }
    }

    private fun onInitUnset(initReport: InitReport, source: PropSource?) {
        unsetValue()
        if (isOptional) return
        if (!isSet && !initReport.ignoreMissing) {
            reportMissingValue(initReport, source)
        }
    }

    private fun reportMissingValue(initReport: InitReport, source: PropSource? = null) {
        initReport.addError(PropError(flatName, source, "missing value"))
    }

    private fun tryLoadingDefault(initReport: InitReport? = null, source: PropSource? = null) {
        if (isSet || defaultProvider == null) return
        val defaultValue = defaultProvider.invoke().castNullable()
        setNullableValueChecking(defaultValue, initReport, source)
    }

    override fun collectLeaves(collect: (PropLeaf) -> Unit) {
        collect(this)
    }

    override val flatName get() = getFlatNameOrThrow()

    override fun formatValue(): String {
        if (propValue == null && !isOptional) return "<undefined>"
        val propValue = propValue ?: return "<null>"
        return formatter(propValue)
    }

    override fun toString(): String {
        return buildString {
            val flatName = _flatName
            if (flatName == null) {
                append("<unnamed>")
            } else {
                append(flatName)
            }
            append(" = ")
            append(formatValue())
        }
    }

    private fun ensureFlatName(thisRef: P) {
        if (_flatName == null) {
            _flatName = "<${thisRef.javaClass.name}>.$name"
        }
    }

    private fun getFlatNameOrThrow(): String {
        return _flatName ?: fetchingUnsetError()
    }

    private fun getInitializedValueOrThrow(): V {
        getFlatNameOrThrow()
        if (!isOptional && !isSet) fetchingUnsetError()
        return propValue.castNullable()
    }

    private fun fetchingUnsetError(): Nothing {
        error("value is not set for key: $name")
    }

    private fun setNullableValueChecking(value: V?, initReport: InitReport? = null, source: PropSource? = null) {
        if (value != null) {
            val checkErrors = checkNonNullValue(value, source)
            if (checkErrors.isNotEmpty()) {
                if (initReport != null) {
                    initReport.addErrors(checkErrors)
                } else {
                    throw InvalidPropsException(checkErrors)
                }
            }
        }

        this.propValue = value
    }

    private fun unsetValue() {
        this.propValue = null
    }

    private fun parseValue(flatValue: String): ParseResult<V> {
        return try {
            val v = parser(flatValue)
            if (v == null) ParseResult.Empty else ParseResult.Value(v)
        } catch (e: Exception) {
            ParseResult.Fail(e)
        }
    }

    /**
     * Must only be called on non-null values.
     */
    private fun checkNonNullValue(value: V, source: PropSource?): List<PropError> {
        checkNotNull(value)

        val errors = mutableListOf<PropError>()
        for (checker in checkers) {
            try {
                val checkPassed = checker.check(value)
                if (!checkPassed) {
                    val msg = checker.message?.invoke(value) ?: "check failed"
                    errors += PropError(flatName, source, msg)
                }
            } catch (e: Exception) {
                val msg = checker.message?.invoke(value) ?: "unexpected exception during check"
                errors += PropError(flatName, source, msg, e)
            }
        }

        return errors
    }

    private sealed class ParseResult<out V> {
        data class Value<V>(val value: V) : ParseResult<V>()
        object Empty : ParseResult<Nothing>()
        data class Fail<V>(val cause: Throwable) : ParseResult<V>()
    }

    companion object {

        private fun <T> T?.castNullable(): T {
            // the following cast always succeeds
            // even if Kotlin type parameter is non-nullable and value is null
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
    }
}
