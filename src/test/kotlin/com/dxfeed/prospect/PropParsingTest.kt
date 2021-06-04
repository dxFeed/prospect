package com.dxfeed.prospect

import com.dxfeed.prospect.ext.parseList
import com.dxfeed.prospect.ext.parseListTo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PropParsingTest {

    data class TestData(val hours: Int, val minutes: Int)

    class PropsCustomParser : Props() {
        val p: TestData
            by property<TestData>()
                .parse(::parseTestData)
    }

    class PropsStringList : Props() {
        val p by property<List<String>>()
            .parseList()
    }

    class PropsCustomList : Props() {
        val p by property<List<TestData>>()
            .parseList { parseTestData(it) }
    }

    class PropsStringSet : Props() {
        val p by property<Set<String>>()
            .parseListTo(List<String>::toSet)
    }

    class PropsCustomSet : Props() {
        val p by property<Set<TestData>>()
            .parseListTo(List<TestData>::toSet) { parseTestData(it) }
    }

    @Test
    fun `Non-nullable property with custom parser`() {
        assertEquals(
            expected = TestData(23, 15),
            actual = PropsCustomParser().load("p" to "23:15").p
        )
    }

    @Test
    fun `Parse List`() {
        assertEquals(
            expected = listOf("a", "y", "e"),
            actual = PropsStringList().load("p" to "a,y,e").p
        )
    }

    @Test
    fun `Parse List with custom parser`() {
        assertEquals(
            expected = listOf(TestData(10, 40), TestData(20, 5)),
            actual = PropsCustomList().load("p" to "10:40, 20:05").p
        )
    }

    @Test
    fun `Parse List into set`() {
        assertEquals(
            expected = setOf("a", "x", "b"),
            actual = PropsStringSet().load("p" to "b,a,x").p
        )
    }

    @Test
    fun `Parse List into set with custom parser`() {
        assertEquals(
            expected = setOf(TestData(10, 20), TestData(8, 59)),
            actual = PropsCustomSet().load("p" to "10:20,8:59,10:20").p
        )
    }

    companion object {

        fun parseTestData(s: String): TestData {
            val (h, m) = s.split(":").map { it.trim().toInt() }
            return TestData(h, m)
        }
    }
}
