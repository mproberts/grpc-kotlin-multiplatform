package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufReader

fun Duration.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            1 -> { seconds = reader.readInt64() }
            
            2 -> { nanos = reader.readFixedInt32() }
            
        }
    }
}
