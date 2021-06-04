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
