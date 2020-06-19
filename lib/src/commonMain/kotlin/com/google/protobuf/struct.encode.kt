package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufWriter
fun Struct.writeTo(writer: ProtobufWriter) {
    
    
    
    

    unknownFields?.let { writer.write(it) }
}

fun Value.writeTo(writer: ProtobufWriter) {
    
    when (kind) {
    	
        is Value.OneOfKind.nullValue -> writer.encode(1, kind.nullValue)
        
        
    	
        is Value.OneOfKind.numberValue -> writer.encode(2, kind.numberValue)
        
        
    	
        is Value.OneOfKind.stringValue -> writer.encode(3, kind.stringValue)
        
        
    	
        is Value.OneOfKind.boolValue -> writer.encode(4, kind.boolValue)
        
        
    	
        is Value.OneOfKind.structValue -> writer.encode(5) { kind.structValue.writeTo(this) }
    	
        
    	
        is Value.OneOfKind.listValue -> writer.encode(6) { kind.listValue.writeTo(this) }
    	
        
    }
    

    unknownFields?.let { writer.write(it) }
}

fun ListValue.writeTo(writer: ProtobufWriter) {
    
    
    
    

    unknownFields?.let { writer.write(it) }
}
