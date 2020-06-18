package dev.mpr.grpc.protobuf.tools

import kotlin.math.min

internal class ConcreteLinkedByteArray(private val bufferSize: Int = defaultBufferSize) : MutableLinkedByteArray {
    internal companion object {
        const val defaultBufferSize = 1024
        const val defaultCacheLimit = 10
    }

    private fun allocate(): ByteArray {
        return if (bufferCache.isEmpty()) {
            ByteArray(bufferSize)
        } else {
            bufferCache.removeAt(bufferCache.size - 1)
        }
    }

    private fun reclaim(bytes: ByteArray) {
        if (bytes.size != bufferSize) {
            // only cache buffers that are of the default size for consistency
            return
        } else if (bufferCache.size > defaultCacheLimit) {
            return
        }

        bufferCache.add(bytes)
    }
    private fun bufferStart(index: Int): Int {
        return if (index == 0) {
            tipOffset
        } else {
            0
        }
    }

    private fun bufferEnd(index: Int): Int {
        return if (index == outputBuffers.size - 1) {
            tailOffset
        } else {
            outputBuffers[index].size
        }
    }

    private fun bufferSize(index: Int): Int {
        return bufferEnd(index) - bufferStart(index)
    }

    private val tempBytes = ByteArray(1)

    private val bufferCache = mutableListOf<ByteArray>()
    private val outputBuffers = mutableListOf(ByteArray(bufferSize))

    private var tipOffset = 0
    private var tailOffset = 0

    private val activeBuffer: ByteArray
        get() = outputBuffers[outputBuffers.size - 1]

    private val availableSpace: Int
        get() = activeBuffer.size - tailOffset

    private var cachedSize: Int = -1

    private fun dirtySize() {
        cachedSize = -1
    }

    override val size: Int
        get() {
            return if (cachedSize >= 0) {
                cachedSize
            } else {
                cachedSize = outputBuffers.fold(0) { sum, nextBuffer ->
                    sum + nextBuffer.size
                } - outputBuffers[outputBuffers.size - 1].size + tailOffset - tipOffset

                cachedSize
            }
        }

    override fun reset() {
        tipOffset = 0
        tailOffset = 0

        bufferCache.addAll(outputBuffers)
        outputBuffers.clear()
        dirtySize()
    }

    override fun clear() {
        reset()
        bufferCache.clear()
    }

    override fun toByteArray(): ByteArray {
        if (outputBuffers.size == 1) {
            return activeBuffer.sliceArray(tipOffset until tailOffset)
        }

        val targetBuffer = ByteArray(size)

        copyTo(targetBuffer)

        return targetBuffer
    }

    override fun copyTo(bytes: ByteArray, destinationOffset: Int, offset: Int, length: Int) {
        var outputOffset = destinationOffset

        if (length > bytes.size) {
            throw IndexOutOfBoundsException("Provided ByteArray too small ${bytes.size} < $length")
        }
        else if (offset + length > size) {
            throw IndexOutOfBoundsException("Requested range too large offset=$offset, length=$length, size=$size")
        }

        var remaining = length
        var skipOffset = offset

        if (outputBuffers.size == 1) {
            activeBuffer.copyInto(bytes, outputOffset, tipOffset + skipOffset, tipOffset + skipOffset + remaining)
        } else {
            val initialAmount = outputBuffers[0].size - tipOffset

            if (skipOffset < initialAmount) {
                val bytesToWrite = min(remaining, initialAmount - skipOffset)
                outputBuffers[0].copyInto(bytes, outputOffset, skipOffset + tipOffset, tipOffset + skipOffset + bytesToWrite)

                skipOffset = 0
                remaining -= bytesToWrite
                outputOffset += bytesToWrite
            } else {
                skipOffset -= initialAmount
            }

            // copy previously completed output
            outputBuffers.forEachIndexed { index, toCopy ->
                when {
                    index == 0 -> {
                        // skip, we've already copied this
                    }
                    index == outputBuffers.size - 1 -> {
                        // skip, we'll copy this later
                    }
                    remaining <= 0 -> {
                        // we're done already, jeeze
                    }
                    skipOffset > toCopy.size -> {
                        // we're not done skipping bytes
                        skipOffset -= toCopy.size
                    }
                    else -> {
                        toCopy.copyInto(bytes, outputOffset, skipOffset, skipOffset + min(
                            remaining,
                            toCopy.size - skipOffset
                        )
                        )

                        remaining -= toCopy.size - skipOffset
                        outputOffset += toCopy.size - skipOffset

                        skipOffset = 0
                    }
                }
            }

            if (remaining > 0) {
                // copy final buffer with any
                activeBuffer.copyInto(bytes, outputOffset, 0, min(tailOffset, remaining))
            }
        }
    }

    override fun write(byte: Byte, destinationOffset: Int) {
        tempBytes[0] = byte

        write(tempBytes, 0, destinationOffset)
    }

    override fun write(bytes: ByteArray, sourceOffset: Int, destinationOffset: Int, length: Int) {
        var remaining = length
        var offset = sourceOffset

        if (length + sourceOffset > bytes.size) {
            throw IndexOutOfBoundsException("Provided bytes too small, ${length + sourceOffset} > ${bytes.size}")
        }

        if (destinationOffset < size) {
            // overwrite the current buffer until we reach the end (if we reach the end)
            var overwriteRemaining = min(size - destinationOffset, length)
            var overwriteOffset = 0
            var bufferOffset = 0
            var overwriteBufferOffset = 0

            while (overwriteOffset < destinationOffset) {
                if (bufferSize(bufferOffset) < destinationOffset - overwriteOffset) {
                    bufferOffset++
                    overwriteOffset += bufferSize(bufferOffset)
                } else {
                    overwriteBufferOffset = if (bufferOffset == 0) {
                        destinationOffset - overwriteOffset + tipOffset
                    }
                    else {
                        destinationOffset - overwriteOffset
                    }
                    break
                }
            }

            while (overwriteRemaining > 0) {
                val bytesWritten = min(overwriteRemaining, bufferEnd(bufferOffset) - overwriteBufferOffset)

                bytes.copyInto(outputBuffers[bufferOffset], overwriteBufferOffset, offset, bytesWritten)

                offset += bytesWritten
                remaining -= bytesWritten
                overwriteRemaining -= bytesWritten
                overwriteBufferOffset = 0

                bufferOffset++
            }
        }

        while (remaining > 0) {
            if (availableSpace >= remaining) {
                // copy completely and we're done
                bytes.copyInto(activeBuffer, tailOffset, offset, offset + remaining)

                tailOffset += remaining
                break
            }

            // copy buffer range
            bytes.copyInto(activeBuffer, tailOffset, offset, offset + availableSpace)

            // allocate a new buffer, continue copying
            remaining -= availableSpace
            offset += availableSpace

            outputBuffers.add(allocate())
            tailOffset = 0
        }

        dirtySize()
    }

    override fun insert(bytes: ByteArray, sourceOffset: Int, destinationOffset: Int, length: Int) {
        // find the split offset
        var remainingDestinationOffset = destinationOffset
        var writeOffset = 0
        var writeBufferOffset = 0

        while (remainingDestinationOffset > 0) {
            if (remainingDestinationOffset > bufferSize(writeBufferOffset)) {
                remainingDestinationOffset -= bufferSize(writeBufferOffset)
                writeBufferOffset++
            } else {
                writeOffset = remainingDestinationOffset
                break
            }
        }

        // split the buffer at the offset into a and b
        val splitBuffer = outputBuffers[writeBufferOffset]

        val headBuffer = splitBuffer.copyOfRange(bufferStart(writeBufferOffset), writeOffset)
        val tailBuffer = splitBuffer.copyOfRange(writeOffset, bufferEnd(writeBufferOffset))
        val wasTip = writeBufferOffset == 0
        val wasTail = writeBufferOffset == outputBuffers.size - 1

        // trim a and b and insert both at the active position
        outputBuffers.removeAt(writeBufferOffset)

        outputBuffers.add(writeBufferOffset, headBuffer)

        // insert the new bytes between a and b
        outputBuffers.add(writeBufferOffset + 1, bytes.copyOfRange(sourceOffset, sourceOffset + length))
        outputBuffers.add(writeBufferOffset + 2, tailBuffer)

        if (wasTip) {
            tipOffset = 0
        }

        if (wasTail) {
            // if the active buffer was split, replace the active buffer with a
            // fresh active buffer with clean state
            tailOffset = 0
            outputBuffers.add(allocate())
        }
    }

    override fun read(bytes: ByteArray, destinationOffset: Int, length: Int) {
        copyTo(bytes, destinationOffset, 0, length)
    }

    override fun advance(length: Int) {
        var advance = length

        while (advance > 0) {
            val currentBuffer = outputBuffers[0]
            val bufferSize = if (outputBuffers.size == 1) {
                tailOffset - tipOffset
            } else {
                currentBuffer.size - tipOffset
            }

            if (advance <= bufferSize) {
                // consume the rest of the advance by moving the tip pointer
                tipOffset += advance
                advance = 0
            } else {
                // consume the next batch of advance by discarding the tip buffer
                advance -= bufferSize

                tipOffset = 0
                reclaim(outputBuffers.removeAt(0))
            }
        }

        if (tipOffset == tailOffset && outputBuffers.size == 1) {
            tipOffset = 0
            tailOffset = 0
        }
        dirtySize()
    }

    override fun read(length: Int) = ByteArray(length).also {
        read(it, 0, length)
    }

    override fun readAdvance(length: Int): ByteArray = read(length).also {
        advance(length)
    }

    override fun readAdvance(bytes: ByteArray, destinationOffset: Int, length: Int) {
        readAdvance(bytes, destinationOffset, length)
        advance(length)
    }

    override fun subarray(offset: Int, length: Int): LinkedByteArray {
        return LinkedByteSubarray(this, offset, length)
    }
}

class LinkedByteSubarray(
    private val parent: LinkedByteArray,
    private val offset: Int,
    private val length: Int
) : LinkedByteArray {

    private var progress = 0

    override val size: Int
        get() = length - progress

    override fun reset() {
        progress = length
    }

    override fun clear() {
        reset()
    }

    override fun toByteArray(): ByteArray = ByteArray(size).also {
        copyTo(it)
    }

    override fun copyTo(bytes: ByteArray, destinationOffset: Int, offset: Int, length: Int) {
        parent.copyTo(bytes, destinationOffset, offset + this.offset + this.progress, length)
    }

    override fun read(bytes: ByteArray, destinationOffset: Int, length: Int) {
        copyTo(bytes, destinationOffset, 0, length)
    }

    override fun read(length: Int): ByteArray = ByteArray(length).also { read(it) }

    override fun readAdvance(length: Int): ByteArray = read(length).also {
        advance(length)
    }

    override fun readAdvance(bytes: ByteArray, destinationOffset: Int, length: Int) {
        read(bytes, destinationOffset, length)
        advance(length)
    }

    override fun advance(length: Int) {
        progress += length
    }

    override fun subarray(offset: Int, length: Int): LinkedByteArray {
        return parent.subarray(offset + this.offset + this.progress, length)
    }
}
