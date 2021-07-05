/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.dxfeed.prospect.ext.propertyOfBoolean
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfList
import com.dxfeed.prospect.ext.propertyOfLong
import com.dxfeed.prospect.ext.propertyOfPassword
import com.dxfeed.prospect.ext.propertyOfString
import com.dxfeed.prospect.ext.propertyOfUrl
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropBuildingTest {

    class PropsOfBoolean : Props() {
        val p by propertyOfBoolean()
    }

    class PropsOfInt : Props() {
        val p by propertyOfInt()
    }

    class PropsOfLong : Props() {
        val p by propertyOfLong()
    }

    class PropsOfString : Props() {
        val p by propertyOfString()
    }

    class PropsOfUrl : Props() {
        val p by propertyOfUrl()
    }

    class PropsOfPassword : Props() {
        val p by propertyOfPassword()
    }

    class PropsOfStringList : Props() {
        val p by propertyOfList()
    }

    class PropsOfNullableStringList : Props() {
        val p: List<String>? by propertyOfList().optional()
    }

    class PropsOfIntList : Props() {
        val p: List<Int> by propertyOfList(transform = String::toInt)
    }

    @Test
    fun `Property of Boolean`() {
        assertEquals(
            expected = true,
            actual = PropsOfBoolean().load("p" to "true").p
        )
    }

    @Test
    fun `Property of Int`() {
        assertEquals(
            expected = 15,
            actual = PropsOfInt().load("p" to "15").p
        )
    }

    @Test
    fun `Property of Long`() {
        assertEquals(
            expected = 100_000_000_000,
            actual = PropsOfLong().load("p" to "100000000000").p
        )
    }

    @Test
    fun `Property of String`() {
        assertEquals(
            expected = "ABC",
            actual = PropsOfString().load("p" to "ABC").p
        )
    }

    @Test
    fun `Property of URL`() {
        PropsOfUrl().load("p" to "https://user:password@example.com")
            .toMultilineString()
            .also {
                assertThat(it).contains("https://user:*****@example.com")
            }
    }

    @Test
    fun `Property of password`() {
        val props = PropsOfPassword().load("p" to "most_secure_password")
        assertEquals(
            expected = "most_secure_password",
            actual = props.p
        )
        val s = props.toMultilineString()
        assertTrue { "most_secure_password" !in s }
    }

    @Test
    fun `Property of string list`() {
        assertEquals(
            expected = emptyList(),
            actual = PropsOfStringList().load().p
        )
        assertEquals(
            expected = listOf("Hello", "World!"),
            actual = PropsOfStringList().load("p" to "Hello, World!").p
        )
    }

    @Test
    fun `String-list is empty when non-optional property is empty`() {
        assertThat(PropsOfStringList().load("p" to "").p)
            .isEqualTo(emptyList())
    }

    @Test
    fun `String-list is empty when optional property is empty`() {
        assertThat(PropsOfNullableStringList().load("p" to "").p)
            .isEqualTo(emptyList())
    }

    @Test
    fun `Property of int list`() {
        assertEquals(
            expected = emptyList(),
            actual = PropsOfIntList().load().p
        )
        assertEquals(
            expected = listOf(2, 7, 27),
            actual = PropsOfIntList().load("p" to " 2,7, 27 ").p
        )
    }
}
