package com.google.protobuf

import dev.mpr.grpc.ProtoDsl

@ProtoDsl
class DurationBuilder {
	constructor()

	constructor(copy: Duration) {
	    builderCopy.seconds = copy.seconds
	    builderCopy.nanos = copy.nanos
	    builderCopy.unknownFields = copy.unknownFields
	}

	private object builderCopy {
	    var seconds: Int = 0
	    var nanos: Int = 0
	    var unknownFields: ByteArray? = null
	}

	fun build(): Duration = Duration(
    	builderCopy.seconds,
    	builderCopy.nanos,
    	builderCopy.unknownFields
	)
	
	var seconds: Int
		set(value) {
			builderCopy.seconds = value
		}

		get() = builderCopy.seconds
	
	
	var nanos: Int
		set(value) {
			builderCopy.nanos = value
		}

		get() = builderCopy.nanos
	
	
}
