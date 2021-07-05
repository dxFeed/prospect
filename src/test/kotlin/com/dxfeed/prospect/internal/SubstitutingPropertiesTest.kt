/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect.internal

import com.dxfeed.prospect.getResourceFile
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SubstitutingPropertiesTest {

    @Test
    fun `Earlier properties are substituted`() {
        System.setProperty("testSystemProp", "test-system")
        assertEquals(
            expected = mapOf(
                "prop1" to "text",
                "prop2" to "a-text-b",
                "prop3" to "a-test-system-b"
            ),
            actual = SubstitutingProperties.readFromFile(getResourceFile("substitute-props.properties"))
        )
    }
}
