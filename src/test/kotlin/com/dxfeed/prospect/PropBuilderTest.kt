@file:Suppress("TestFunctionName")

package com.dxfeed.prospect

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.dxfeed.prospect.ext.propertyOfBoolean
import com.dxfeed.prospect.ext.propertyOfDouble
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfLong
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PropBuilderTest {

    class PropsEmpty : Props()

    @Test
    fun `empty props load successfully`() {
        PropsEmpty().load()
    }

    class PropsBoolean : Props() {
        val p: Boolean by propertyOfBoolean()
    }

    @Test
    fun `Non-nullable boolean`() {
        assertEquals(
            expected = true,
            actual = PropsBoolean().load("p" to "true").p
        )
        assertEquals(
            expected = true,
            actual = PropsBoolean().load("p" to "TRUE").p
        )
        assertEquals(
            expected = false,
            actual = PropsBoolean().load("p" to "false").p
        )
        assertEquals(
            expected = false,
            actual = PropsBoolean().load("p" to "FALSE").p
        )
        assertEquals(
            expected = false,
            actual = PropsBoolean().load("p" to "blah").p
        )
    }

    class PropsNullableBoolean : Props() {
        val p: Boolean? by propertyOfBoolean().optional()
    }

    @Test
    fun `Nullable boolean`() {
        assertEquals(
            expected = true,
            actual = PropsNullableBoolean().load("p" to "true").p
        )
        assertEquals(
            expected = null as Boolean?,
            actual = PropsNullableBoolean().load().p
        )
    }

    class PropsString : Props() {
        val p: String by propertyOfString()
    }

    @Test
    fun `Non-nullable string`() {
        assertEquals(
            expected = "abc",
            actual = PropsString().load("p" to "abc").p
        )
    }

    class PropsNullableString : Props() {
        val p: String? by propertyOfString().optional()
    }

    @Test
    fun `Nullable string`() {
        assertEquals(
            expected = "zyx",
            actual = PropsNullableString().load("p" to "zyx").p
        )
        assertEquals(
            expected = null as String?,
            actual = PropsNullableString().load().p
        )
    }

    @Test
    fun `Empty value for nullable string is replaced with null`() {
        val strNull = null as String?
        assertEquals(expected = strNull, actual = PropsNullableString().load("p" to "").p)
    }

    class PropsInt : Props() {
        val n: Int by propertyOfInt()
    }

    @Test
    fun `Non-nullable int`() {
        assertEquals(
            expected = 14,
            actual = PropsInt().load("n" to "14").n
        )
    }

    @Test
    fun `Loading non-parsable int fails`() {
        assertFailsWith<InvalidPropsException> {
            PropsInt().load("n" to "hey")
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("n")
                    prop(PropError::message).isEqualTo("unable to parse value from 'hey'")
                }
            }
        }
    }

    class PropsNullableInt : Props() {
        val n: Int? by propertyOfInt().optional()
    }

    @Test
    fun `Nullable int`() {
        assertEquals(
            expected = 14,
            actual = PropsNullableInt().load("n" to "14").n
        )
        assertEquals(
            expected = null as Int?,
            actual = PropsNullableInt().load().n
        )
    }

    @Test
    fun `Empty value for nullable int is replaced with null`() {
        val intNull = null as Int?
        assertEquals(expected = intNull, actual = PropsNullableInt().load("n" to "").n)
    }

    class PropsLong : Props() {
        val n: Long by propertyOfLong()
    }

    @Test
    fun `Non-nullable long`() {
        assertEquals(
            expected = 14,
            actual = PropsLong().load("n" to "14").n
        )
        assertEquals(
            expected = -3,
            actual = PropsLong().load("n" to "-3").n
        )
    }

    class PropsNullableLong : Props() {
        val n: Long? by propertyOfLong().optional()
    }

    @Test
    fun `Nullable long`() {
        assertEquals(
            expected = 100500,
            actual = PropsNullableLong().load("n" to "100500").n
        )
        assertEquals(
            expected = null as Long?,
            actual = PropsNullableLong().load().n
        )
    }

    class PropsDouble : Props() {
        val n: Double by propertyOfDouble()
    }

    @Test
    fun `Non-nullable double`() {
        assertEquals(
            expected = 200.123,
            actual = PropsDouble().load("n" to "200.123").n
        )
    }

    class PropsNullableDouble : Props() {
        val n: Double? by propertyOfDouble().optional()
    }

    @Test
    fun `Nullable double`() {
        assertEquals(
            expected = 14.28,
            actual = PropsNullableDouble().load("n" to "14.28").n
        )
        assertEquals(
            expected = null as Double?,
            actual = PropsNullableDouble().load().n
        )
    }

    enum class TestEnum { ABC }

    class PropsEnum : Props() {
        val p by property<TestEnum>().parse { TestEnum.valueOf(it) }
    }

    @Test
    fun `Non-nullable enum`() {
        assertEquals(
            expected = TestEnum.ABC,
            actual = PropsEnum().load("p" to "ABC").p
        )
    }

    class LazyDefaultProps : Props() {
        val p: Int by propertyOfInt().withDefault { 42 }
    }

    @Test
    fun `Prop with lazy default`() {
        assertEquals(
            expected = 42,
            actual = LazyDefaultProps().load().p
        )
    }
}
