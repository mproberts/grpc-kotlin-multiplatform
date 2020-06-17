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
    val nullValue: NullValue = NullValue.NULL_VALUE,
    val numberValue: Double = 0.0,
    val stringValue: String = "",
    val boolValue: Boolean = false,
    val structValue: Struct? = null,
    val listValue: ListValue? = null,
    val unknownFields: ByteArray? = null
) {
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

enum class NullValue {
    NULL_VALUE;
}
