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
        if (bufferCache.size > defaultCacheLimit) {
            return
        }

        bufferCache.add(bytes)
    }

    private val bufferCache = mutableListOf<ByteArray>()
    private val outputBuffers = mutableListOf(ByteArray(bufferSize))

    private var tipOffset = 0
    private var tailOffset = 0

    private val activeBuffer: ByteArray
        get() = outputBuffers[outputBuffers.size - 1]

    private val availableSpace: Int
        get() = activeBuffer.size - tailOffset

    override val size: Int
        get() = outputBuffers.fold(0) { sum, nextBuffer ->
            sum + nextBuffer.size
        } - outputBuffers[outputBuffers.size - 1].size + tailOffset - tipOffset

    override fun reset() {
        tipOffset = 0
        tailOffset = 0

        bufferCache.addAll(outputBuffers)
        outputBuffers.clear()
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

    override fun write(bytes: ByteArray, sourceOffset: Int, length: Int) {
        var remaining = length
        var offset = sourceOffset

        if (length + sourceOffset > bytes.size) {
            throw IndexOutOfBoundsException("Provided bytes too small, ${length + sourceOffset} > ${bytes.size}")
        }

        while (remaining > 0) {
            if (availableSpace >= remaining) {
                // copy completely and we're done
                bytes.copyInto(activeBuffer, tailOffset, offset, offset + remaining)

                tailOffset += remaining
                remaining = 0
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
