package dev.mpr.grpc.protobuf.tools

actual object SerializationTools {
    actual fun readString(bytes: ByteArray): String {
        return String(bytes)
    }

    actual fun writeString(string: String): ByteArray {
        return string.toByteArray(Charsets.UTF_8)
    }
}
