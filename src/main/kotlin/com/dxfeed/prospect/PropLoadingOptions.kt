package com.dxfeed.prospect

public data class PropLoadingOptions(
    /**
     * Unknown property is the one which is not declared, but is present during loading.
     */
    val ignoreUnknown: Boolean = false,
    /**
     * Missing property is a required property which is not present during loading.
     */
    val ignoreMissing: Boolean = false
) {

    public fun merge(other: PropLoadingOptions): PropLoadingOptions {
        return PropLoadingOptions(
            ignoreUnknown = ignoreUnknown || other.ignoreUnknown,
            ignoreMissing = ignoreMissing || other.ignoreMissing
        )
    }

    public companion object {
        public val DEFAULT: PropLoadingOptions = PropLoadingOptions()
        public val IGNORE_UNKNOWN: PropLoadingOptions = PropLoadingOptions(ignoreUnknown = true)
        public val IGNORE_MISSING: PropLoadingOptions = PropLoadingOptions(ignoreMissing = true)
    }
}
