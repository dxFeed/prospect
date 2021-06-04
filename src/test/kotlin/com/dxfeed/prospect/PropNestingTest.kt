package com.dxfeed.prospect

import com.dxfeed.prospect.ext.propertyOfInt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropNestingTest {

    class PropsSub : Props() {
        val x by propertyOfInt()
    }

    class PropsMain : Props() {
        @Suppress("unused")
        val main by propertyOfInt().withDefault(101)
        val sub by nested<PropsSub> { PropsSub() }
    }

    class PropsSubWithParam(default: Int) : Props() {
        val x by propertyOfInt()
            .withDefault(default)
    }

    class PropsMainCustomNested : Props() {
        val sub by nested { PropsSubWithParam(-1) }
    }

    class PropsMainIncluded : Props() {
        val sub by included<PropsSub> { PropsSub() }
    }

    @Test
    fun `Nested properties initialised from parameterless constructor`() {
        assertEquals(
            expected = 57,
            actual = PropsMain().load("sub.x" to "57").sub.x
        )
    }

    @Test
    fun `Nested properties with custom creator`() {
        assertEquals(
            expected = -1,
            actual = PropsMainCustomNested().load().sub.x
        )
    }

    @Test
    fun `Included properties`() {
        assertEquals(
            expected = 80,
            actual = PropsMainIncluded().load("x" to "80").sub.x
        )
    }
}
