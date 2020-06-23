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
        infix fun String.to(value: Value) {
            builderCopy.fields = builderCopy.fields + Pair(this, value)
        }

        fun value(key: String, builder: ValueBuilder.() -> Unit) {
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
        builderCopy.kind = copy.kind
        builderCopy.unknownFields = copy.unknownFields
    }

    private object builderCopy {
        var kind: Value.OneOfKind? = null
        var unknownFields: ByteArray? = null
    }

    fun build(): Value = Value(
        builderCopy.kind,
        builderCopy.unknownFields
    )
    
    @ProtoDsl
    inner class OneOfkindBuilder {
        var nullValue: NullValue?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.nullValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.nullValue)?.nullValue
            
        
        var numberValue: Double?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.numberValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.numberValue)?.numberValue
            
        
        var stringValue: String?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.stringValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.stringValue)?.stringValue
            
        
        var boolValue: Boolean?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.boolValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.boolValue)?.boolValue
            
        
        var structValue: Struct?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.structValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.structValue)?.structValue
            
            fun structValue(builder: StructBuilder.() -> Unit) {
                builderCopy.kind = StructBuilder().apply(builder).build().let { Value.OneOfKind.structValue(it) }
            }
        
        var listValue: ListValue?
            set(value) {
                builderCopy.kind = value?.let { Value.OneOfKind.listValue(it) }
            }

            get() = (builderCopy.kind as? Value.OneOfKind.listValue)?.listValue
            
            fun listValue(builder: ListValueBuilder.() -> Unit) {
                builderCopy.kind = ListValueBuilder().apply(builder).build().let { Value.OneOfKind.listValue(it) }
            }
        
    }

    fun kind(builder: OneOfkindBuilder.() -> Unit) {
        OneOfkindBuilder().apply(builder)
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
