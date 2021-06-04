package com.dxfeed.prospect

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.dxfeed.prospect.ext.checkInRange
import com.dxfeed.prospect.ext.checkMatches
import com.dxfeed.prospect.ext.checkNotNegative
import com.dxfeed.prospect.ext.checkPositive
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PropCheckingTest {

    class PropsIntPositive : Props() {
        val n: Int by propertyOfInt().checkPositive()
    }

    class PropsIntNotNegative : Props() {
        val n: Int by propertyOfInt().checkNotNegative()
    }

    class PropsIntInRange : Props() {
        val n by propertyOfInt().checkInRange(1..23)
    }

    class PropsStringMatches : Props() {
        val s by propertyOfString().checkMatches("bo.*om".toRegex())
    }

    class MutPropsChecked : Props() {
        var p: String by propertyOfString().check("check1") { false }
    }

    @Test
    fun `Int passes check for being positive`() {
        assertEquals(
            expected = 2,
            actual = PropsIntPositive().load("n" to "2").n
        )
    }

    @Test
    fun `Int does not pass check for being positive`() {
        assertFailsWith<InvalidPropsException> {
            PropsIntPositive().load("n" to "0")
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("n")
                    prop(PropError::message).isEqualTo("expected a positive number")
                }
            }
        }
    }

    @Test
    fun `Int passes check for being non-negative`() {
        assertEquals(
            expected = 0,
            actual = PropsIntNotNegative().load("n" to "0").n
        )
    }

    @Test
    fun `Int does not pass check for being non-negative`() {
        assertFailsWith<InvalidPropsException> {
            PropsIntNotNegative().load("n" to "-1")
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("n")
                    prop(PropError::message).isEqualTo("expected a non-negative number")
                }
            }
        }
    }

    @Test
    fun `Int passes check for being in range`() {
        assertEquals(
            expected = 10,
            actual = PropsIntInRange().load("n" to "10").n
        )
    }

    @Test
    fun `String passes regex check`() {
        assertEquals(
            expected = "boom",
            actual = PropsStringMatches().load("s" to "boom").s
        )
        assertEquals(
            expected = "bo000om",
            actual = PropsStringMatches().load("s" to "bo000om").s
        )
    }

    @Test
    fun `String does not pass regex check`() {
        assertFailsWith<InvalidPropsException> {
            PropsStringMatches().load("s" to "noob")
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("s")
                    prop(PropError::message).isEqualTo("expected value to match regex bo.*om")
                }
            }
        }
    }

    @Test
    fun `Mutable property is check on set`() {
        assertFailsWithExceptionMsg(
            exceptionMsgSubstring = "check1"
        ) {
            MutPropsChecked().apply {
                p = "test-value"
            }
        }
    }
}
