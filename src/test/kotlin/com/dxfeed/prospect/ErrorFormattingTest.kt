/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import com.dxfeed.prospect.ext.propertyOfInt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ErrorFormattingTest {

    @Suppress("unused") // used by reporting
    class BadCheckProps : Props() {
        val p0: Int by propertyOfInt("p0").check("error0") { false }
        val p1: Int by propertyOfInt("p1").check("error1") { false }
        val p2: Int by propertyOfInt("p2").check("error2") { false }
    }

    @Test
    fun `Error formatting`() {
        val flatProps = mapOf("p0" to "0").toFlatProps()
            .merge(mapOf("p1" to "1").toFlatProps(source = "source1"))
            .merge(mapOf("p2" to "2").toFlatProps(source = "source2"))

        assertFailsWith<InvalidPropsException> {
            BadCheckProps().load(flatProps)
        }.also { t ->
            assertEquals(
                expected = """
                    Found 3 errors:
                    <unknown source>:
                    ${"\t"}p0: error0
                    source1:
                    ${"\t"}p1: error1
                    source2:
                    ${"\t"}p2: error2
                """.trimIndent(),
                actual = t.message
            )
        }
    }
}
