package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufWriter
fun Timestamp.writeTo(writer: ProtobufWriter) {
    
    
    if (seconds != 0L) writer.encode(1, seconds)
    
    
    if (nanos != 0) writer.encode(2, nanos)
    

    unknownFields?.let { writer.write(it) }
}
