package com.dxfeed.prospect

public class PropError(
    public val flatName: FlatName,
    public val source: PropSource? = null,
    public val message: String,
    public val cause: Throwable? = null,
) {
    override fun toString(): String = buildString {
        append("PropError[").append(flatName).append("]")
        append("(message=").append(message)
        append(", source=").append(source)
        append(", cause=").append(cause?.stackTraceToString())
        append(")")
    }
}
