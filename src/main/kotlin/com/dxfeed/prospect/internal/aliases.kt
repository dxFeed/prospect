package com.dxfeed.prospect.internal

internal typealias PropParser<V> = (value: String) -> V?

internal typealias PropDefaulter<V> = () -> V

internal typealias PropFormatter<V> = (value: V) -> String
