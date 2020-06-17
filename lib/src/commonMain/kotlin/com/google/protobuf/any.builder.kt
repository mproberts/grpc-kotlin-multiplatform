package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class AnyBuilder {
	constructor()

	constructor(copy: Any) {
	    builderCopy.typeUrl = copy.typeUrl
	    builderCopy.value = copy.value
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    var typeUrl: String = ""
	    var value: ByteArray = ByteArray(0)
	    var unknownFields: ByteArray? = null
	}

	fun build(): Any = Any(
    	builderCopy.typeUrl,
    	builderCopy.value,
    	builderCopy.unknownFields
	)
	
	var typeUrl: String
		set(value) {
			builderCopy.typeUrl = value
		}

		get() = builderCopy.typeUrl
	
	
	var value: ByteArray
		set(value) {
			builderCopy.value = value
		}

		get() = builderCopy.value
	
	
}
