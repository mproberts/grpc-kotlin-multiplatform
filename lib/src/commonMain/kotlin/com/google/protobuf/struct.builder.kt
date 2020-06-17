package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class StructBuilder {
	constructor()

	constructor(copy: Struct) {
	    builderCopy.fields = copy.fields
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    var fields: Map<String, Value> = emptyMap()
	    var unknownFields: ByteArray? = null
	}

	fun build(): Struct = Struct(
    	builderCopy.fields,
    	builderCopy.unknownFields
	)
	
	var fields: Map<String, Value>
		set(value) {
			builderCopy.fields = value
		}

		get() = builderCopy.fields
	
	@ProtoDsl
	inner class fieldsMapBuilder {
		infix fun String.to(builder: ValueBuilder.() -> Unit) {
			builderCopy.fields = builderCopy.fields + Pair(this, ValueBuilder().apply(builder).build())
		}

		fun fields(key: String, builder: ValueBuilder.() -> Unit) {
			key to ValueBuilder().apply(builder).build()
		}
	}

	fun fields(builder: fieldsMapBuilder.() -> Unit) {
		fieldsMapBuilder().apply(builder)
	}
	
}

@ProtoDsl
class ValueBuilder {
	constructor()

	constructor(copy: Value) {
	    builderCopy.nullValue = copy.nullValue
	    builderCopy.numberValue = copy.numberValue
	    builderCopy.stringValue = copy.stringValue
	    builderCopy.boolValue = copy.boolValue
	    builderCopy.structValue = copy.structValue
	    builderCopy.listValue = copy.listValue
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    var nullValue: NullValue = NullValue.NULL_VALUE
	    var numberValue: Double = 0.0
	    var stringValue: String = ""
	    var boolValue: Boolean = false
	    var structValue: Struct? = null
	    var listValue: ListValue? = null
	    var unknownFields: ByteArray? = null
	}

	fun build(): Value = Value(
    	builderCopy.nullValue,
    	builderCopy.numberValue,
    	builderCopy.stringValue,
    	builderCopy.boolValue,
    	builderCopy.structValue,
    	builderCopy.listValue,
    	builderCopy.unknownFields
	)
	
	var nullValue: NullValue
		set(value) {
			builderCopy.nullValue = value
		}

		get() = builderCopy.nullValue
	
	
	var numberValue: Double
		set(value) {
			builderCopy.numberValue = value
		}

		get() = builderCopy.numberValue
	
	
	var stringValue: String
		set(value) {
			builderCopy.stringValue = value
		}

		get() = builderCopy.stringValue
	
	
	var boolValue: Boolean
		set(value) {
			builderCopy.boolValue = value
		}

		get() = builderCopy.boolValue
	
	
	var structValue: Struct?
		set(value) {
			builderCopy.structValue = value
		}

		get() = builderCopy.structValue
	
	fun structValue(builder: StructBuilder.() -> Unit) {
		builderCopy.structValue = StructBuilder().apply(builder).build()
	}
	
	var listValue: ListValue?
		set(value) {
			builderCopy.listValue = value
		}

		get() = builderCopy.listValue
	
	fun listValue(builder: ListValueBuilder.() -> Unit) {
		builderCopy.listValue = ListValueBuilder().apply(builder).build()
	}
	
}

@ProtoDsl
class ListValueBuilder {
	constructor()

	constructor(copy: ListValue) {
	    builderCopy.values = copy.values
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    var values: List<Value> = emptyList()
	    var unknownFields: ByteArray? = null
	}

	fun build(): ListValue = ListValue(
    	builderCopy.values,
    	builderCopy.unknownFields
	)
	
	var values: List<Value>
		set(value) {
			builderCopy.values = value
		}

		get() = builderCopy.values
	
	@ProtoDsl
	inner class valuesListBuilder {
		fun add(value: Value) {
			builderCopy.values = builderCopy.values + value
		}
		
		fun addValue(builder: ValueBuilder.() -> Unit) {
			add(ValueBuilder().apply(builder).build())
		}
	}

	fun values(builder: valuesListBuilder.() -> Unit) {
		valuesListBuilder().apply(builder)
	}
	
}
