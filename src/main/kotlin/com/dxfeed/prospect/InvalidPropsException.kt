package com.dxfeed.prospect

public class InvalidPropsException(public val errors: List<PropError>) :
    RuntimeException(buildCompoundErrorMessage(errors))
