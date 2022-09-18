package dev.mpr.grpc.protobuf.tools

internal object ProtoConstants {
    object Flags {
        const val TagType = 3
        const val TagTypeBitCount = 3
    }

    object WireType {
        const val varInt: Int = 0
        const val fixed64: Int = 1
        const val lengthDelimited: Int = 2
        const val fixed32: Int = 5
    }

    fun fieldNumberFromTag(tag: Int): Int {
        return tag.ushr(Flags.TagType)
    }

    fun wireTypeFromTag(tag: Int): Int {
        return tag and Flags.TagType
    }
}

interface ProtobufWriter {
    val length: Int

    fun encode(fieldNumber: Int, value: ByteArray)

    fun encode(fieldNumber: Int, value: String)

    fun encode(fieldNumber: Int, value: Boolean)

    fun encode(fieldNumber: Int, value: Int, signed: Boolean = false)

    fun encode(fieldNumber: Int, value: Float)

    fun encode(fieldNumber: Int, value: Long, signed: Boolean = false)

    fun encode(fieldNumber: Int, value: UInt)

    fun encode(fieldNumber: Int, value: ULong)

    fun encode(fieldNumber: Int, value: Double)

    fun <T : Enum<T>> encode(fieldNumber: Int, value: T)

    fun encode(fieldNumber: Int, builder: ProtobufWriter.() -> Unit)

    fun encode(value: ByteArray)

    fun encode(value: String)

    fun encode(value: Boolean)

    fun encode(value: Int, signed: Boolean = false)

    fun encode(value: Float)

    fun encode(value: Long, signed: Boolean = false)

    fun encode(value: UInt)

    fun encode(value: ULong)

    fun encode(value: Double)

    fun <T : Enum<T>> encode(value: T)

    fun encode(builder: ProtobufWriter.() -> Unit)

    fun write(bytes: ByteArray)
}

open class ScopedProtobufWriter(private val output: MutableLinkedByteArray) : ProtobufWriter {
    private val tempBuffer = ByteArray(8)
    override var length: Int = 0

    private fun fieldTag(fieldNumber: Int, wireType: Int): Int {
        return fieldNumber shl ProtoConstants.Flags.TagType or wireType
    }

    private fun writeByte(value: Byte) {
        output.write(value)
        length++
    }

    private fun writeBytes(value: ByteArray, sourceLength: Int = value.size) {
        output.write(value, length = sourceLength)
        length += sourceLength
    }

    override fun encode(fieldNumber: Int, value: ByteArray) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.lengthDelimited))

        encode(value)
    }

    override fun encode(fieldNumber: Int, value: String) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.lengthDelimited))
        encode(value)
    }

    override fun encode(fieldNumber: Int, value: Boolean) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value)
    }

    override fun encode(fieldNumber: Int, value: Int, signed: Boolean) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value, signed)
    }

    override fun encode(fieldNumber: Int, value: Float) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.fixed32))
        encode(value)
    }

    override fun encode(fieldNumber: Int, value: Long, signed: Boolean) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value, signed)
    }

    override fun encode(fieldNumber: Int, value: UInt) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value)
    }

    override fun encode(fieldNumber: Int, value: ULong) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value)
    }

    override fun encode(fieldNumber: Int, value: Double) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.fixed64))
        encode(value)
    }

    override fun <T : Enum<T>> encode(fieldNumber: Int, value: T) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        encode(value)
    }

    override fun encode(value: ByteArray) {
        writeRawVarInt32(value.size)
        writeBytes(value)
    }

    override fun encode(value: String) {
        writeBytes(SerializationTools.writeString(value))
    }

    override fun encode(value: Boolean) {
        writeRawVarInt32(if (value) 1 else 0)
    }

    override fun encode(value: Int, signed: Boolean) {
        writeRawVarInt32(if (signed) {
            (value shl 1) xor (value shr 31)
        } else {
            value
        })
    }

    override fun encode(value: Float) {
        writeRawInt32(value.toRawBits())
    }

    override fun encode(value: Long, signed: Boolean) {
        writeRawVarInt64(if (signed) {
            (value shl 1) xor (value shr 63)
        } else {
            value
        })
    }

    override fun encode(value: UInt) {
        writeRawVarInt32(value.toInt())
    }

    override fun encode(value: ULong) {
        writeRawVarInt64(value.toLong())
    }

    override fun encode(value: Double) {
        writeRawInt64(value.toRawBits())
    }

    override fun <T : Enum<T>> encode(value: T) {
        writeRawVarInt32(value.ordinal)
    }





    private fun writeRawInt32(value: Int) {
        var pendingWrite = value
        var offset = 0
        for (i in 1..4) {
            tempBuffer[offset++] = (pendingWrite and 0xFF).toByte()
            pendingWrite = pendingWrite ushr 8
        }

        writeBytes(tempBuffer, offset)
    }

    private fun writeRawInt64(value: Long) {
        var pendingWrite = value
        var offset = 0
        for (i in 1..8) {
            tempBuffer[offset++] = (pendingWrite and 0xFF).toByte()
            pendingWrite = pendingWrite ushr 8
        }

        writeBytes(tempBuffer, offset)
    }

    private fun writeRawVarInt32(value: Int) {
        var pendingWrite = value
        var offset = 0
        while (true) {
            if (pendingWrite and 0x7F.inv() == 0) {
                tempBuffer[offset++] = pendingWrite.toByte()
                break
            } else {
                tempBuffer[offset++] = (pendingWrite and 0x7F or 0x80).toByte()
                pendingWrite = pendingWrite ushr 7
            }
        }

        writeBytes(tempBuffer, offset)
    }

    private fun writeRawVarInt64(value: Long) {
        TODO()
    }

    private fun computeRawVarint32Size(value: Int): Int {
        if (value and (-0x1 shl 7) == 0)
            return 1
        if (value and (-0x1 shl 14) == 0)
            return 2
        if (value and (-0x1 shl 21) == 0)
            return 3
        return if (value and (-0x1 shl 28) == 0) 4 else 5
    }

    override fun encode(fieldNumber: Int, builder: ProtobufWriter.() -> Unit) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.lengthDelimited))

        // reserve space for where the length will go
        val messageLengthOffset = output.size

        // reserve space for the length
        writeByte(0x00)

        val messageLength = with(ScopedProtobufWriter(output)) {
            builder()
            length
        }

        val sizeOfSize = computeRawVarint32Size(messageLength)

        if (sizeOfSize > 1) {
            // make room for the unexpectedly large length value
            output.insert(ByteArray(sizeOfSize - 1), 0, messageLengthOffset)
        }

        // write the message length into the buffer at the reserved position
        var value = messageLength
        var lengthOffset = messageLengthOffset

        while (true) {
            if (value and 0x7F.inv() == 0) {
                output.write(value.toByte(), lengthOffset)
                break
            } else {
                output.write((value and 0x7F or 0x80).toByte(), lengthOffset)
                lengthOffset++
                value = value ushr 7
            }
        }

        length += sizeOfSize - 1 + messageLength
    }

    override fun write(bytes: ByteArray) {
        output.write(bytes)

        length += bytes.size
    }
}

class ProtobufOutputStream(bufferSize: Int = 1024) {

    private val output = MutableLinkedByteArray(bufferSize)
    private val writer = ScopedProtobufWriter(output)

    fun write(encoder: ProtobufWriter.() -> Unit) {
        writer.encoder()
    }

    fun toByteArray(): ByteArray {
        return output.toByteArray()
    }
}
