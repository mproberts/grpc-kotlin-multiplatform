package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufWriter
fun DoubleValue.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0.0) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun FloatValue.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0.0f) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun Int64Value.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0L) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun UInt64Value.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0UL) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun Int32Value.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun UInt32Value.writeTo(writer: ProtobufWriter) {
    
    
    if (value != 0U) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun BoolValue.writeTo(writer: ProtobufWriter) {
    
    
    if (value != false) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun StringValue.writeTo(writer: ProtobufWriter) {
    
    
    if (value != "") writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}

fun BytesValue.writeTo(writer: ProtobufWriter) {
    
    
    if (value.isNotEmpty()) writer.encode(1, value)
    

    unknownFields?.let { writer.write(it) }
}
