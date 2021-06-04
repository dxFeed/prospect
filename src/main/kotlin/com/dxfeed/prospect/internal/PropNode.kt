package com.dxfeed.prospect.internal

import com.dxfeed.prospect.FlatName
import com.dxfeed.prospect.FlatProps
import com.dxfeed.prospect.PropError

internal interface PropNode {

    val name: String

    fun init(
        parentPrefix: String,
        flatProps: FlatProps,
        initReport: InitReport
    )

    fun collectLeaves(collect: (PropLeaf) -> Unit)
}

internal interface PropLeaf : PropNode {

    val hasFlatName: Boolean

    val flatName: FlatName

    fun formatValue(): String
}

internal class InitReport(
    loadFlatNames: Collection<FlatName>,
    val ignoreMissing: Boolean
) {

    private val _unknown = loadFlatNames.toMutableSet()

    val unknown: Set<FlatName> get() = _unknown
    val errors: MutableList<PropError> = mutableListOf()

    fun markSeen(name: FlatName) {
        _unknown -= name
    }

    fun addError(error: PropError) {
        this.errors += error
    }

    fun addErrors(errors: List<PropError>) {
        this.errors += errors
    }
}
