package com.dxfeed.prospect

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.assertions.support.appendName
import kotlin.test.assertFails
import kotlin.test.fail

/**
 * Asserts that given function [block] fails by throwing an exception.
 * Then checks if the thrown exception message contains given substring.
 */
fun assertFailsWithExceptionMsg(
    exceptionMsgSubstring: String,
    exceptionCauseMsgSubstring: String? = null,
    block: () -> Unit
): Throwable {

    val e = assertFails(block)
    if (exceptionMsgSubstring.isEmpty()) throw e

    val msg = e.message ?: fail("\nExpected exception to have a message: $e")
    assertContains(msg, exceptionMsgSubstring)

    if (exceptionCauseMsgSubstring != null) {
        val cause = e.cause
            ?: fail("Expected exception cause")

        val causeMsg = cause.message
            ?: fail("\nExpected cause-exception to have a message: $e")

        assertContains(causeMsg, exceptionCauseMsgSubstring)
    }

    return e
}

fun Assert<PropError>.missingValue(flatName: String) {
    all {
        prop(PropError::flatName).isEqualTo(flatName)
        prop(PropError::message).isEqualTo("missing value")
    }
}

@Suppress("DEPRECATION")
fun <T> Assert<assertk.Result<T>>.invalidProps(): Assert<List<PropError>> {
    return isFailure()
        .isInstanceOf(InvalidPropsException::class)
        .prop(InvalidPropsException::errors)
}

fun <T> Assert<List<T>>.hasSingleItem(): Assert<T> {
    return transform(appendName("[0]", separator = ".")) {
        assertThat(it.size, "size").isEqualTo(1)
        it.first()
    }
}

private fun assertContains(actual: String, expectedSubstring: String) {
    if (!actual.contains(expectedSubstring)) {
        fail("\nExpected substring: $expectedSubstring\nActual: $actual")
    }
}

fun <T : Any> T.getResourceFile(resourceName: String): String {
    return javaClass.classLoader.getResource(resourceName)
        ?.file?.toString()
        ?: fail("resource not found: $resourceName")
}
