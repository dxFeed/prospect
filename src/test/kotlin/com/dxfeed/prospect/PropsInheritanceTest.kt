/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropsInheritanceTest {

    open class ParentProps : Props() {
        val a by propertyOfString()
    }

    class ChildProps : ParentProps() {
        val b by propertyOfInt()
    }

    @Test
    fun `Child properties inherit parent properties`() {
        val props = ChildProps().load(
            "a" to "parent",
            "b" to "18"
        )

        assertEquals(
            expected = 18,
            actual = props.b
        )
        assertEquals(
            expected = "parent",
            actual = props.a
        )
    }
}
