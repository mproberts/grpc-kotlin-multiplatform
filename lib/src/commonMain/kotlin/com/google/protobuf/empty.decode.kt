package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufReader

fun Empty.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
        }
    }
}
