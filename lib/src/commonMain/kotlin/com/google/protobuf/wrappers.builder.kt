package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class DoubleValueBuilder {
    constructor()

    constructor(copy: DoubleValue) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: Double = 0.0
        var unknownFields: ByteArray? = null
    }

    fun build(): DoubleValue = DoubleValue(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: Double
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class FloatValueBuilder {
    constructor()

    constructor(copy: FloatValue) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: Float = 0.0f
        var unknownFields: ByteArray? = null
    }

    fun build(): FloatValue = FloatValue(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: Float
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class Int64ValueBuilder {
    constructor()

    constructor(copy: Int64Value) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: Long = 0L
        var unknownFields: ByteArray? = null
    }

    fun build(): Int64Value = Int64Value(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: Long
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class UInt64ValueBuilder {
    constructor()

    constructor(copy: UInt64Value) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: ULong = 0UL
        var unknownFields: ByteArray? = null
    }

    fun build(): UInt64Value = UInt64Value(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: ULong
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class Int32ValueBuilder {
    constructor()

    constructor(copy: Int32Value) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: Int = 0
        var unknownFields: ByteArray? = null
    }

    fun build(): Int32Value = Int32Value(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: Int
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class UInt32ValueBuilder {
    constructor()

    constructor(copy: UInt32Value) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: UInt = 0U
        var unknownFields: ByteArray? = null
    }

    fun build(): UInt32Value = UInt32Value(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: UInt
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class BoolValueBuilder {
    constructor()

    constructor(copy: BoolValue) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: Boolean = false
        var unknownFields: ByteArray? = null
    }

    fun build(): BoolValue = BoolValue(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: Boolean
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class StringValueBuilder {
    constructor()

    constructor(copy: StringValue) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: String = ""
        var unknownFields: ByteArray? = null
    }

    fun build(): StringValue = StringValue(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: String
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}

@ProtoDsl
class BytesValueBuilder {
    constructor()

    constructor(copy: BytesValue) {
        builderCopy.value = copy.value
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var value: ByteArray = ByteArray(0)
        var unknownFields: ByteArray? = null
    }

    fun build(): BytesValue = BytesValue(
        builderCopy.value,
        builderCopy.unknownFields
    )
    
    
    var value: ByteArray
        set(value) {
            builderCopy.value = value
        }

        get() = builderCopy.value
    
    
}
