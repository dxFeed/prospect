@file:Suppress("ClassName")

package com.dxfeed.prospect

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LoadingTest {

    class NonOptProps : Props() {
        val p by property<String>("p").parse { it }
    }

    @Nested
    inner class `Non-optional property without default` {

        @Test
        fun `Flat value is null`() {
            assertThat { NonOptProps().load() }
                .invalidProps()
                .hasSingleItem()
                .missingValue("p")
        }

        @Test
        fun `Flat value is empty`() {
            assertThat { NonOptProps().load("p" to "") }
                .invalidProps()
                .hasSingleItem()
                .missingValue("p")
        }

        @Test
        fun `Flat value is non-empty`() {
            assertThat(NonOptProps().load("p" to "wow"))
                .prop(NonOptProps::p).isEqualTo("wow")
        }
    }

    class NonOptWithDefaultProps : Props() {
        val p by property<String>("p").parse { it }.withDefault { "<def>" }
    }

    @Nested
    inner class `Non-optional property with default` {

        @Test
        fun `Flat value is null`() {
            assertThat(NonOptWithDefaultProps().load())
                .prop(NonOptWithDefaultProps::p).isEqualTo("<def>")
        }

        @Test
        fun `Flat value is empty`() {
            assertThat { NonOptWithDefaultProps().load("p" to "") }
                .invalidProps()
                .hasSingleItem()
                .missingValue("p")
        }

        @Test
        fun `Flat value is non-empty`() {
            assertThat(NonOptWithDefaultProps().load("p" to "wow"))
                .prop(NonOptWithDefaultProps::p).isEqualTo("wow")
        }
    }

    class OptProps : Props() {
        val p: String? by property<String>("p").parse { it }.optional()
    }

    @Nested
    inner class `Optional property without default` {

        @Test
        fun `Flat value is null`() {
            assertThat(OptProps().load())
                .prop(OptProps::p).isNull()
        }

        @Test
        fun `Flat value is empty`() {
            assertThat(OptProps().load("p" to ""))
                .prop(OptProps::p).isNull()
        }

        @Test
        fun `Flat value is non-empty`() {
            assertThat(OptProps().load("p" to "wow"))
                .prop(OptProps::p).isEqualTo("wow")
        }
    }

    class OptWithDefaultProps : Props() {
        val p: String? by property<String>("p").parse { it }.withDefault { "<def>" }.optional()
    }

    @Nested
    inner class `Optional property with default` {

        @Test
        fun `Flat value is null`() {
            assertThat(OptWithDefaultProps().load())
                .prop(OptWithDefaultProps::p).isEqualTo("<def>")
        }

        @Test
        fun `Flat value is empty`() {
            assertThat(OptWithDefaultProps().load("p" to ""))
                .prop(OptWithDefaultProps::p).isNull()
        }

        @Test
        fun `Flat value is non-empty`() {
            assertThat(OptWithDefaultProps().load("p" to "wow"))
                .prop(OptWithDefaultProps::p).isEqualTo("wow")
        }
    }

    class NonOptUnsettingParserProps : Props() {
        val p by property<String>("p").parse { if (it == "-") null else it }
    }

    @Nested
    inner class `Non-optional property with unsetting parser` {

        @Test
        fun `Flat value is non-empty`() {
            assertThat(NonOptUnsettingParserProps().load("p" to "wow"))
                .prop(NonOptUnsettingParserProps::p).isEqualTo("wow")
        }

        @Test
        fun `Flat value is non-empty but unset`() {
            assertThat { NonOptUnsettingParserProps().load("p" to "-") }
                .invalidProps()
                .hasSingleItem()
                .missingValue("p")
        }
    }

    class OptUnsettingParserProps : Props() {
        val p: String? by property<String>("p").parse { if (it == "-") null else it }.optional()
    }

    @Nested
    inner class `Optional property with unsetting parser` {

        @Test
        fun `Flat value is non-empty`() {
            assertThat(OptUnsettingParserProps().load("p" to "wow"))
                .prop(OptUnsettingParserProps::p).isEqualTo("wow")
        }

        @Test
        fun `Flat value is non-empty but unset`() {
            assertThat(OptUnsettingParserProps().load("p" to "-"))
                .prop(OptUnsettingParserProps::p).isNull()
        }
    }

    class NonOptWithDefaultOnEmptyProps : Props() {
        val p by property<String>("p").parse { it }
            .withDefault(useOnEmpty = true) { "<def>" }
    }

    @Nested
    inner class `Non-optional property using default on empty` {

        @Test
        fun `Flat value is empty`() {
            assertThat(NonOptWithDefaultOnEmptyProps().load("p" to ""))
                .prop(NonOptWithDefaultOnEmptyProps::p).isEqualTo("<def>")
        }
    }

    class OptWithDefaultOnEmptyProps : Props() {
        val p: String? by property<String>("p").parse { it }
            .withDefault(useOnEmpty = true) { "<def>" }
            .optional()
    }

    @Nested
    inner class `Optional property using default on empty` {

        @Test
        fun `Flat value is empty`() {
            assertThat(OptWithDefaultOnEmptyProps().load("p" to ""))
                .prop(OptWithDefaultOnEmptyProps::p).isEqualTo("<def>")
        }
    }
}
