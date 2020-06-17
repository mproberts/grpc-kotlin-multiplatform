package com.google.protobuf


data class Timestamp(
    val seconds: Int = 0,
    val nanos: Int = 0,
    val unknownFields: ByteArray? = null
) {
    companion object {
		fun build(builder: TimestampBuilder.() -> Unit): Timestamp {
			return TimestampBuilder().apply(builder).build()
		}
    }

	fun copyBuild(builder: TimestampBuilder.() -> Unit): Timestamp {
		return TimestampBuilder(this).apply(builder).build()
	}
}
