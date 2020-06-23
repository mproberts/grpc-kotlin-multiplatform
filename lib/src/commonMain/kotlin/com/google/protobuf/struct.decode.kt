package com.google.protobuf

import dev.mpr.grpc.protobuf.tools.ProtobufReader

fun Struct.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            
            
        }
    }
}

fun Value.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            
            
	        2 -> {
	            kind {
                    numberValue = reader.readDouble()
                }
            }
            
	        3 -> {
	            kind {
                    stringValue = reader.readString()
                }
            }
            
	        4 -> {
	            kind {
                    boolValue = reader.readBool()
                }
            }
            
	        5 -> {
	            kind {
	                structValue = reader.readField { fieldReader ->
	                    Struct.readFrom(fieldReader)
	                }
                }
            }
            
            
	        6 -> {
	            kind {
	                listValue = reader.readField { fieldReader ->
	                    ListValue.readFrom(fieldReader)
	                }
                }
            }
            
            
        
        }
    }
}

fun ListValue.Companion.readFrom(reader: ProtobufReader) = build {
    while (reader.nextField()) {
        when (reader.currentFieldNumber) {
	    
            
            
        }
    }
}
