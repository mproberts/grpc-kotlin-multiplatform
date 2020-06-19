package com.google.protobuf


data class DoubleValue(
    val value: Double = 0.0,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: DoubleValueBuilder.() -> Unit): DoubleValue {
            return DoubleValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: DoubleValueBuilder.() -> Unit): DoubleValue {
        return DoubleValueBuilder(this).apply(builder).build()
    }
}

data class FloatValue(
    val value: Float = 0.0f,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: FloatValueBuilder.() -> Unit): FloatValue {
            return FloatValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: FloatValueBuilder.() -> Unit): FloatValue {
        return FloatValueBuilder(this).apply(builder).build()
    }
}

data class Int64Value(
    val value: Long = 0L,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: Int64ValueBuilder.() -> Unit): Int64Value {
            return Int64ValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: Int64ValueBuilder.() -> Unit): Int64Value {
        return Int64ValueBuilder(this).apply(builder).build()
    }
}

data class UInt64Value(
    val value: ULong = 0UL,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: UInt64ValueBuilder.() -> Unit): UInt64Value {
            return UInt64ValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: UInt64ValueBuilder.() -> Unit): UInt64Value {
        return UInt64ValueBuilder(this).apply(builder).build()
    }
}

data class Int32Value(
    val value: Int = 0,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: Int32ValueBuilder.() -> Unit): Int32Value {
            return Int32ValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: Int32ValueBuilder.() -> Unit): Int32Value {
        return Int32ValueBuilder(this).apply(builder).build()
    }
}

data class UInt32Value(
    val value: UInt = 0U,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: UInt32ValueBuilder.() -> Unit): UInt32Value {
            return UInt32ValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: UInt32ValueBuilder.() -> Unit): UInt32Value {
        return UInt32ValueBuilder(this).apply(builder).build()
    }
}

data class BoolValue(
    val value: Boolean = false,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: BoolValueBuilder.() -> Unit): BoolValue {
            return BoolValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: BoolValueBuilder.() -> Unit): BoolValue {
        return BoolValueBuilder(this).apply(builder).build()
    }
}

data class StringValue(
    val value: String = "",
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: StringValueBuilder.() -> Unit): StringValue {
            return StringValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: StringValueBuilder.() -> Unit): StringValue {
        return StringValueBuilder(this).apply(builder).build()
    }
}

data class BytesValue(
    val value: ByteArray = ByteArray(0),
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: BytesValueBuilder.() -> Unit): BytesValue {
            return BytesValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: BytesValueBuilder.() -> Unit): BytesValue {
        return BytesValueBuilder(this).apply(builder).build()
    }
}
