package dev.mpr.grpc.protobuf.tools

actual object SerializationTools {
    actual fun readString(bytes: ByteArray): String = bytes.decodeToString()

    actual fun writeString(string: String): ByteArray = string.encodeToByteArray()
}