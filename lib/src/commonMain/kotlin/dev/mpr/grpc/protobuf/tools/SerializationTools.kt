package dev.mpr.grpc.protobuf.tools

expect object SerializationTools {
    fun readString(bytes: ByteArray): String

    fun writeString(string: String): ByteArray
}