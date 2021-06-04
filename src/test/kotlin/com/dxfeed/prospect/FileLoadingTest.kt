package com.dxfeed.prospect

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileLoadingTest {

    class LoadingTestProps : Props() {
        val p by propertyOfInt("p")
        val extra by propertyOfString("extra").withDefault("")
    }

    @Test
    fun `Loaded from properties file`() {
        assertEquals(
            expected = 17,
            actual = LoadingTestProps().loadFromFile(
                propertiesFile = getResourceFile("props-no-override.properties")
            ).p
        )
    }

    @Test
    fun `Loaded from properties file when override file is not specified`() {
        assertEquals(
            expected = 17,
            actual = LoadingTestProps().loadOverridableFromFile(
                propertiesFile = getResourceFile("props-no-override.properties"),
                propertiesOverrideFile = null
            ).p
        )
    }

    @Test
    fun `Loaded from properties file when override file does not exist`() {
        val propsFile = getResourceFile("props-with-override.properties")
        assertEquals(
            expected = 28,
            actual = LoadingTestProps().loadOverridableFromFile(
                propertiesFile = propsFile,
                propertiesOverrideFile = "$propsFile.does-not-exist"
            ).p
        )
    }

    @Test
    fun `Loaded from override file when properties file does not exist`() {
        val overrideFile = getResourceFile("props-with-override.properties.override")
        val props = LoadingTestProps().loadOverridableFromFile(
            propertiesFile = "$overrideFile.does-not-exist",
            propertiesOverrideFile = overrideFile
        )
        assertEquals(
            expected = 101,
            actual = props.p
        )
        assertEquals(
            expected = "abc",
            actual = props.extra
        )
    }

    @Test
    fun `Loaded from override file when both files exist`() {
        val propsFile = getResourceFile("props-with-override.properties")
        val overrideFile = getResourceFile("props-with-override.properties.override")
        val props = LoadingTestProps().loadOverridableFromFile(
            propertiesFile = propsFile,
            propertiesOverrideFile = overrideFile
        )
        assertEquals(
            expected = 101,
            actual = props.p
        )
        assertEquals(
            expected = "abc",
            actual = props.extra
        )
    }

    @Test
    fun `Loaded from properties file in sys props when override file is not specified`() {
        val sysProp = "loading-test"
        System.setProperty(sysProp, getResourceFile("props-no-override.properties"))
        assertEquals(
            expected = 17,
            actual = LoadingTestProps().loadOverridableFromFileInSysProp(
                propertiesFileSysProp = sysProp,
                propertiesOverrideFileSysProp = null
            ).p
        )
    }

    @Test
    fun `Loaded from override file in sys props when both sys props specified and files exist`() {
        val sysProp = "loading-test"
        val sysPropOverride = "$sysProp.override"
        System.setProperty(sysProp, getResourceFile("props-with-override.properties"))
        System.setProperty(sysPropOverride, getResourceFile("props-with-override.properties.override"))
        assertEquals(
            expected = 101,
            actual = LoadingTestProps().loadOverridableFromFileInSysProp(
                propertiesFileSysProp = sysProp,
                propertiesOverrideFileSysProp = sysPropOverride
            ).p
        )
    }

    @Test
    fun `Does not throw when required property is present`() {
        LoadingTestProps().load(
            "p" to "101"
        )
    }

    @Test
    fun `Failure when prop is missing`() {
        assertFailsWith<InvalidPropsException> {
            LoadingTestProps().load()
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("p")
                    prop(PropError::message).isEqualTo("missing value")
                }
            }
        }
    }

    @Test
    fun `Does not throw when required property is missing but missing are ignored`() {
        LoadingTestProps().load(options = PropLoadingOptions.IGNORE_MISSING)
    }

    @Test
    fun `Failure for unknown and missing props`() {
        assertFailsWith<InvalidPropsException> {
            LoadingTestProps().load(
                "definitely-unknown" to "not even parsed"
            )
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(2)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("p")
                    prop(PropError::message).isEqualTo("missing value")
                }
                index(1).all {
                    prop(PropError::flatName).isEqualTo("definitely-unknown")
                    prop(PropError::message).isEqualTo("unknown key")
                }
            }
        }
    }

    @Test
    fun `Failure for unknown props`() {
        assertFailsWith<InvalidPropsException> {
            LoadingTestProps().load(
                "p" to "100",
                "definitely-unknown" to "not even parsed",
            )
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("definitely-unknown")
                    prop(PropError::message).isEqualTo("unknown key")
                }
            }
        }
    }

    @Test
    fun `Does not throw when property is unknown but unknowns are ignored`() {
        LoadingTestProps().load(
            "p" to "100",
            "definitely-unknown" to "not even parsed",
            options = PropLoadingOptions.IGNORE_UNKNOWN
        )
    }

    @Test
    fun `Does not throw when one property is unknown, another is missing, but unknown and missing are ignored`() {
        LoadingTestProps().load(
            "definitely-unknown" to "not even parsed",
            options = PropLoadingOptions.IGNORE_UNKNOWN.merge(PropLoadingOptions.IGNORE_MISSING)
        )
    }

    @Test
    fun `Source is included in error report`() {
        assertFailsWith<InvalidPropsException> {
            LoadingTestProps().load(
                "p" to "100",
                "definitely-unknown" to "not even parsed",
                source = "prop-source"
            )
        }.also { t ->
            assertThat(t.errors, "errors").all {
                hasSize(1)
                index(0).all {
                    prop(PropError::flatName).isEqualTo("definitely-unknown")
                    prop(PropError::source).isEqualTo("prop-source")
                    prop(PropError::message).isEqualTo("unknown key")
                }
            }
        }
    }
}
