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
