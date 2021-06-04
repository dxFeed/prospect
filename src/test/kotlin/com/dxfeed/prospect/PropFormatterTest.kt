/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import com.dxfeed.prospect.ext.formatAsPassword
import com.dxfeed.prospect.ext.formatAsUrl
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test

class PropFormatterTest {

    class PropsMaskPass : Props() {
        val p by propertyOfString()
            .formatAsPassword()
    }

    class PropsMaskUrl : Props() {
        @Suppress("unused")
        val p by propertyOfString()
            .formatAsUrl()
    }

    class PropsCustomMask : Props() {
        val p by propertyOfInt().format { it.toString().replace('1', '#') }
    }

    @Test
    fun `Masked password`() {
        val ps = PropsMaskPass().load("p" to "secret")
        assertThat(ps.p).isEqualTo("secret")
        assertThat(ps.toMultilineString()).doesNotContain("secret")
    }

    @Test
    fun `Masked url`() {
        PropsMaskUrl().load("p" to "https://user:pass-word@wow.com/path")
            .toMultilineString()
            .also {
                assertThat(it).contains("https://user:*****@wow.com/path")
            }

        PropsMaskUrl().load("p" to "user:pass@wow.com/path")
            .toMultilineString()
            .also {
                assertThat(it).contains("user:*****@wow.com/path")
            }

        PropsMaskUrl().load("p" to "http://localhost:5432/path")
            .toMultilineString()
            .also {
                assertThat(it).contains("http://localhost:5432/path")
            }

        PropsMaskUrl().load("p" to "localhost:5432/path")
            .toMultilineString()
            .also {
                assertThat(it).contains("localhost:5432/path")
            }
    }

    @Test
    fun `Custom-masked`() {
        val ps = PropsCustomMask().load("p" to "10101")
        assertThat(ps.p).isEqualTo(10101)
        assertThat(ps.toMultilineString()).contains("p = #0#0#")
    }

    class MultilineFormatProps : Props() {
        val p by propertyOfString()
    }

    @Test
    fun `Multiline props`() {
        val props = MultilineFormatProps().load("p" to "a\nb")
        assertThat(props.p).isEqualTo("a\nb")
        assertThat(props.toMultilineString())
            .isEqualTo("p = a\nb")
    }
}
