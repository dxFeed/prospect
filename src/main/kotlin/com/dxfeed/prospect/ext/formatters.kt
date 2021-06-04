package com.dxfeed.prospect.ext

import com.dxfeed.prospect.PropBuilder

/**
 * Replaces the value with `"*****"` when formatting.
 */
public fun <V : Any> PropBuilder<V>.formatAsPassword(): PropBuilder<V> = format { "*****" }

/**
 * When there is a user and a password in the URL, the password is replaced with
 * `"*****"` when formatting.
 */
public fun <V : Any> PropBuilder<V>.formatAsUrl(): PropBuilder<V> {
    val re = "^([^:]+://)?([^:]+):([^@]+)@".toRegex()
    return format { it.toString().replaceFirst(re, "$1$2:*****@") }
}
