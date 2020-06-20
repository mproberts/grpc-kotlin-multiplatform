package dev.mpr.grpc.protobuf.tools

import com.google.protobuf.StringValue
import com.google.protobuf.Value
import kotlin.experimental.or

interface ProtobufReader {
    val currentFieldNumber: Int

    fun nextField(): Boolean

    fun <T> readField(builder: (ProtobufReader) -> T): T

    fun readBytes(): ByteArray

    fun readString(): String

    fun readBool(): Boolean

    fun readSInt32(): Int

    fun readSInt64(): Long

    fun readInt32(): Int

    fun readInt64(): Long

    fun readUInt32(): UInt

    fun readUInt64(): ULong

    fun readDouble(): Double

    fun readFloat(): Float

    fun readFixedInt32(): Int

    fun readFixedInt64(): Long
}

class ScopedProtobufReader(
    private val input: LinkedByteArray
) : ProtobufReader {
    private val tempBuffer = ByteArray(8)

    private var currentFieldType: Int = 0

    override var currentFieldNumber: Int = 0
        get() = field
        private set(value) {
            field = value
        }

    private fun nextByte(): Byte {
        if (input.size == 0) {
            throw IndexOutOfBoundsException("Message length exceeded")
        }

        input.readAdvance(tempBuffer, length = 1)

        return tempBuffer[0]
    }

    override fun nextField(): Boolean {
        if (input.size == 0) {
            return false
        }

        val tag = readInt32()

        currentFieldNumber = ProtoConstants.fieldNumberFromTag(tag)
        currentFieldType = ProtoConstants.wireTypeFromTag(tag)

        return true
    }

    override fun <T> readField(builder: (ProtobufReader) -> T): T {
        val expectedLength = readInt32()

        return builder(ScopedProtobufReader(input.subarray(0, expectedLength))).also {
            input.advance(expectedLength)
        }
    }

    override fun readBytes(): ByteArray {
        return input.readAdvance(length = readInt32())
    }

    private fun decodeString(bytes: ByteArray): String {
        TODO()
    }

    override fun readString(): String {
        return decodeString(readBytes())
    }

    override fun readBool(): Boolean {
        return readInt32() == 1
    }

    override fun readSInt32(): Int {
        TODO("Not yet implemented")
    }

    override fun readSInt64(): Long {
        TODO("Not yet implemented")
    }

    override fun readFixedInt32(): Int {
        input.readAdvance(tempBuffer, length = 4)

        return (tempBuffer[0].toInt() shl 24) or
                (tempBuffer[1].toInt() shl 16) or
                (tempBuffer[2].toInt() shl 8) or
                (tempBuffer[3].toInt())
    }

    override fun readFixedInt64(): Long {
        input.readAdvance(tempBuffer, length = 8)

        return (tempBuffer[0].toLong() shl 56) or
                (tempBuffer[1].toLong() shl 48) or
                (tempBuffer[2].toLong() shl 40) or
                (tempBuffer[3].toLong() shl 32) or
                (tempBuffer[4].toLong() shl 24) or
                (tempBuffer[5].toLong() shl 16) or
                (tempBuffer[6].toLong() shl 8) or
                (tempBuffer[7].toLong())
    }

    override fun readInt32(): Int {
        var tmp: Int = nextByte().toInt()

        if (tmp >= 0) {
            return tmp
        }
        var result: Int = tmp and 0x7f

        tmp = nextByte().toInt()
        if (tmp >= 0) {
            result = result or (tmp shl 7)
        }
        else {
            result = result or (tmp and 0x7f) shl 7
            tmp = nextByte().toInt()
            if (tmp >= 0) {
                result = result or (tmp shl 14)
            }
            else {
                result = result or ((tmp and 0x7f) shl 14)
                tmp = nextByte().toInt()
                if (tmp >= 0) {
                    result = result or (tmp shl 21)
                }
                else {
                    result = result or ((tmp and 0x7f) shl 21)

                    tmp = nextByte().toInt()
                    result = result or (tmp shl 28)
                    if (tmp < 0) {
                        // Discard upper 32 bits.
                        for (i in 0 until 5) {
                            tmp = nextByte().toInt()
                            if (tmp >= 0) {
                                return result
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    override fun readFloat(): Float = Float.fromBits(readFixedInt32())

    override fun readDouble(): Double = Double.fromBits(readFixedInt64())

    override fun readInt64(): Long {
        TODO("Not yet implemented")
    }

    override fun readUInt32(): UInt {
        TODO("Not yet implemented")
    }

    override fun readUInt64(): ULong {
        TODO("Not yet implemented")
    }
}
