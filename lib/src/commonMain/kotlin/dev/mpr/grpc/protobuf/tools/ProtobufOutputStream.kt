package dev.mpr.grpc.protobuf.tools

internal object ProtoConstants {
    object Flags {
        const val TagType = 3
    }

    object WireType {
        const val varInt: Int = 0
        const val fixed64: Int = 1
        const val lengthDelimited: Int = 2
        const val fixed32: Int = 5
    }
}

interface ProtobufWriter {
    val length: Int

    fun encode(fieldNumber: Int, value: ByteArray)

    fun encode(fieldNumber: Int, value: Boolean)

    fun encode(fieldNumber: Int, value: Int)

    fun encode(fieldNumber: Int, value: Float)

    fun <T : Enum<T>> encode(fieldNumber: Int, value: T)

    fun encodeMessage(fieldNumber: Int, builder: ProtobufWriter.() -> Unit)
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

        writeRawVarInt32(value.size)
        writeBytes(value)
    }

    override fun encode(fieldNumber: Int, value: Boolean) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        writeRawVarInt32(if (value) 1 else 0)
    }

    override fun encode(fieldNumber: Int, value: Int) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        writeRawVarInt32(value)
    }

    override fun encode(fieldNumber: Int, value: Float) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        writeRawVarInt32(value.toRawBits())
    }

    override fun <T : Enum<T>> encode(fieldNumber: Int, value: T) {
        writeRawVarInt32(fieldTag(fieldNumber, ProtoConstants.WireType.varInt))
        writeRawVarInt32(value.ordinal)
    }

    private fun writeRawVarInt32(value: Int) {
        var value = value
        var offset = 0
        while (true) {
            if (value and 0x7F.inv() == 0) {
                tempBuffer[offset++] = value.toByte()
                break
            } else {
                tempBuffer[offset++] = (value and 0x7F or 0x80).toByte()
                value = value ushr 7
            }
        }

        writeBytes(tempBuffer, offset)
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

    override fun encodeMessage(fieldNumber: Int, builder: ProtobufWriter.() -> Unit) {
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
        println("size of size $sizeOfSize = $messageLength")
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

    // TODO: long, double, uint, ulong
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
