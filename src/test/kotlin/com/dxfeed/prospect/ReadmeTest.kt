/*
 * Copyright (C) 2002 - 2021 Devexperts LLC
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.dxfeed.prospect

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.dxfeed.prospect.ext.checkMatches
import com.dxfeed.prospect.ext.checkNotEmpty
import com.dxfeed.prospect.ext.checkPositive
import com.dxfeed.prospect.ext.propertyOfBoolean
import com.dxfeed.prospect.ext.propertyOfInt
import com.dxfeed.prospect.ext.propertyOfList
import com.dxfeed.prospect.ext.propertyOfPassword
import com.dxfeed.prospect.ext.propertyOfString
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.test.assertEquals

class ReadmeTest {

    @Test
    fun `Readme test reads an example file`() {
        @Suppress("UNCHECKED_CAST")
        val properties: Map<String, String> = """
            verbose=true
            worker.count=64
            levels=1:1:2:3:5
            endpoint.login=admin@acme
            endpoint.password=admin
        """.trimIndent()
            .reader().use { Properties().apply { load(it) } }
            .let { (it as Map<String, String>).toMap() }

        val config = AppConfig().load(properties.toFlatProps())

        assertThat(config).all {
            prop(AppConfig::verbose).isEqualTo(true)
            prop(AppConfig::workerCount).isEqualTo(64)
            prop(AppConfig::levels).isEqualTo(listOf(1, 1, 2, 3, 5))
            prop(AppConfig::endpoint).all {
                prop(EndpointConfig::login).isEqualTo("admin@acme")
                prop(EndpointConfig::password).isEqualTo("admin")
            }
        }

        assertEquals(
            expected = """
                verbose = true
                worker.count = 64
                levels = [1, 1, 2, 3, 5]
                endpoint.login = admin@acme
                endpoint.password = *****
            """.trimIndent(),
            actual = config.toMultilineString()
        )
    }
}

class AppConfig : Props() {

    /**
     * Expects a flag property called `verbose`,
     * using default value of false if property is not provided.
     *
     * Name of the property is taken from the name of the class member.
     */
    val verbose: Boolean
            by propertyOfBoolean(default = false)

    /**
     * Expects a required numeric property called `worker.count`,
     * checking if provided value is positive and if provided value is
     * a power of 2.
     */
    val workerCount: Int
            by propertyOfInt("worker.count").checkPositive()
                .check("expected power of 2") { it.countOneBits() == 1 }

    /**
     * Expects a required property called `levels`.
     *
     * Property string is split by custom separator,
     * and each item is parsed by a given parser.
     *
     * Additionally, the list is checked to be not empty.
     */
    val levels: List<Int>
            by propertyOfList(separator = ":") { it.toInt() }
                .checkNotEmpty()

    /**
     * More properties are nested with prefix `endpoint`.
     * In this case the resulting properties are:
     * - `endpoint.login`
     * - `endpoint.password`
     */
    val endpoint: EndpointConfig
            by nested("endpoint") { EndpointConfig() }
}

class EndpointConfig : Props() {

    /**
     * Expects a required string value.
     *
     * Checks that the value conforms to a given regex.
     */
    val login: String
            by propertyOfString("login")
                .checkMatches(".*@acme$".toRegex())

    /**
     * Expects an optional string value, that is masked with `*****`
     * whenever the properties are formatted for printing.
     */
    val password: String?
            by propertyOfPassword("password").optional()
}
