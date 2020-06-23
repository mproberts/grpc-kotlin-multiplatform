package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufReader

fun DoubleValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readDouble() }
            
        }
    }
}

fun FloatValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readFloat() }
            
        }
    }
}

fun Int64Value.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readInt64() }
            
        }
    }
}

fun UInt64Value.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readUInt64() }
            
        }
    }
}

fun Int32Value.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readFixedInt32() }
            
        }
    }
}

fun UInt32Value.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readUInt32() }
            
        }
    }
}

fun BoolValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readBool() }
            
        }
    }
}

fun StringValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { value = reader.readString() }
            
        }
    }
}

fun BytesValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            
            
        }
    }
}
