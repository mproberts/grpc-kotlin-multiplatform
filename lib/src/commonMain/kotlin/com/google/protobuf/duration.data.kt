package com.google.protobuf


data class Duration(
    val seconds: Long = 0L,
    val nanos: Int = 0,
    val unknownFields: ByteArray? = null
) {
    companion object {
        fun build(builder: DurationBuilder.() -> Unit): Duration {
            return DurationBuilder().apply(builder).build()
        }
    }

    fun copyBuild(builder: DurationBuilder.() -> Unit): Duration {
        return DurationBuilder(this).apply(builder).build()
    }
}
