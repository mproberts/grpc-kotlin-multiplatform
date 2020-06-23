package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class TimestampBuilder {
    constructor()

    constructor(copy: Timestamp) {
        builderCopy.seconds = copy.seconds
        builderCopy.nanos = copy.nanos
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var seconds: Long = 0L
        var nanos: Int = 0
        var unknownFields: ByteArray? = null
    }

    fun build(): Timestamp = Timestamp(
        builderCopy.seconds,
        builderCopy.nanos,
        builderCopy.unknownFields
    )
    
    
    var seconds: Long
        set(value) {
            builderCopy.seconds = value
        }

        get() = builderCopy.seconds
    
    
    var nanos: Int
        set(value) {
            builderCopy.nanos = value
        }

        get() = builderCopy.nanos
    
    
}
