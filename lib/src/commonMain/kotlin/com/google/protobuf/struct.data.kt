package com.google.protobuf


data class Struct(
    val fields: Map<String, Value> = emptyMap(),
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: StructBuilder.() -> Unit): Struct {
            return StructBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: StructBuilder.() -> Unit): Struct {
        return StructBuilder(this).apply(builder).build()
    }
}

data class Value(
    val kind: OneOfKind? = null,
    val unknownFields: ByteArray? = null
) {
    sealed class OneOfKind {
        data class nullValue(val nullValue: NullValue) : OneOfKind()
        data class numberValue(val numberValue: Double) : OneOfKind()
        data class stringValue(val stringValue: String) : OneOfKind()
        data class boolValue(val boolValue: Boolean) : OneOfKind()
        data class structValue(val structValue: Struct) : OneOfKind()
        data class listValue(val listValue: ListValue) : OneOfKind()
    }
    
    companion object {
        fun build(builder: ValueBuilder.() -> Unit): Value {
            return ValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: ValueBuilder.() -> Unit): Value {
        return ValueBuilder(this).apply(builder).build()
    }
}

data class ListValue(
    val values: List<Value> = emptyList(),
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: ListValueBuilder.() -> Unit): ListValue {
            return ListValueBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: ListValueBuilder.() -> Unit): ListValue {
        return ListValueBuilder(this).apply(builder).build()
    }
}

enum class NullValue(val value: Int) {
    NULL_VALUE(0);
}
