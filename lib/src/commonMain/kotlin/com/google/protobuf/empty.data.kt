package com.google.protobuf


data class Empty(
    val unknownFields: ByteArray? = null
) {
    companion object {
		fun build(builder: EmptyBuilder.() -> Unit): Empty {
			return EmptyBuilder().apply(builder).build()
		}
    }

	fun copyBuild(builder: EmptyBuilder.() -> Unit): Empty {
		return EmptyBuilder(this).apply(builder).build()
	}
}
