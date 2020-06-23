package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class EmptyBuilder {
    constructor()

    constructor(copy: Empty) {
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var unknownFields: ByteArray? = null
    }

    fun build(): Empty = Empty(
        builderCopy.unknownFields
    )
    
    
}
