package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufWriter
fun Any.writeTo(writer: ProtobufWriter) {
    
    
    if (typeUrl != "") writer.encode(1, typeUrl)
    
    
    if (value.isNotEmpty()) writer.encode(2, value)
    

    unknownFields?.let { writer.write(it) }
}
