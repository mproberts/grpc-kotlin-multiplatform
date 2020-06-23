package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufReader

fun Any.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { typeUrl = reader.readString() }
            
            
            
        }
    }
}
