package com.dxfeed.prospect

private const val NAME_SEP = "."

internal fun String.appendNestedName(name: String): String {
    return when {
        this.isEmpty() -> name
        name.isEmpty() -> this
        else -> "$this$NAME_SEP$name"
    }
}
