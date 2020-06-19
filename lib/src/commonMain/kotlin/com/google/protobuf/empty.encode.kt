package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufWriter
fun Empty.writeTo(writer: ProtobufWriter) {
    

    unknownFields?.let { writer.write(it) }
}
