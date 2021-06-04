package com.dxfeed.prospect.internal

internal class PropChecker<in V>(
    val message: ((V) -> String)? = null,
    val check: (V) -> Boolean
)
