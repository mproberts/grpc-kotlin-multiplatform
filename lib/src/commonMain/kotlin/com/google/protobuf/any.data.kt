package com.google.protobuf


data class Any(
    val typeUrl: String = "",
    val value: ByteArray = ByteArray(0),
    val unknownFields: ByteArray? = null
) {
    companion object {
		fun build(builder: AnyBuilder.() -> Unit): Any {
			return AnyBuilder().apply(builder).build()
		}
    }

	fun copyBuild(builder: AnyBuilder.() -> Unit): Any {
		return AnyBuilder(this).apply(builder).build()
	}
}
