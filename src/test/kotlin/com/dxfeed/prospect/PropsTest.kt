@file:Suppress("TestFunctionName")

/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropsTest {

    class PropsString : Props() {
        val p: String by propertyOfString()
    }

    class PropsStringDefault : Props() {
        val p by propertyOfString()
            .withDefault("neutral")
    }

    class MutProps : Props() {
        var p: String by propertyOfString()
    }

    @Test
    fun `Second load overrides values`() {
        val ps = PropsString()

        ps.load("p" to "abc")
        assertEquals(
            expected = "abc",
            actual = ps.p
        )

        ps.load("p" to "overridden")
        assertEquals(
            expected = "overridden",
            actual = ps.p
        )
    }

    @Test
    fun `Property with default requires load`() {
        assertThat { PropsStringDefault().p }
            .isFailure()
            .isInstanceOf(IllegalStateException::class)
            .hasMessage("value is not set for key: p")
    }

    @Test
    fun `Set property at runtime`() {
        val props = MutProps().apply {
            p = "mutable"
        }

        assertEquals(
            expected = "mutable",
            actual = props.p
        )
    }
}
