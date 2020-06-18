package dev.mpr.grpc

import dev.mpr.grpc.protobuf.tools.MutableLinkedByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private val hexCharLookup = "0123456789abcdef"

fun ByteArray.toHexString(space: Boolean = false) = fold(StringBuilder()) { builder, byte ->
    if (space && builder.isNotEmpty()) {
        builder.append(' ')
    }

    builder.append(byte.toUByte().toString(16).padStart(2, '0'))

    builder
}.toString()

fun String.toByteArray(): ByteArray {
    val stripped = this.replace(" ", "")
    val result = ByteArray(stripped.length / 2)

    for (i in 0 until stripped.length step 2) {
        val firstIndex = hexCharLookup.indexOf(stripped[i]);
        val secondIndex = hexCharLookup.indexOf(stripped[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i / 2] = (octet and 0xff).toByte()
    }

    return result
}

class LinkedByteArrayTests {

    lateinit var bytes: MutableLinkedByteArray

    @BeforeTest
    fun setup() {
        // use a small block size to exacerbate edge conditions
        bytes = MutableLinkedByteArray(bufferSize = 8)
    }

    private fun verifyWrite(source: String) {
        bytes.write(source.toByteArray())

        assertEquals(source, bytes.toByteArray().toHexString(true))
    }

    @Test
    fun `Less than 1 block worth`() = verifyWrite("ff ff ff")

    @Test
    fun `1 block worth`()  = verifyWrite("01 02 03 04 05 06 07 08")

    @Test
    fun `Overflow 1 block`() = verifyWrite("01 02 03 04 05 06 07 08 09")

    @Test
    fun `Overflow multiple blocks`() = verifyWrite(
        "01 02 03 04 05 06 07 08 09 " +
        "01 02 03 04 05 06 07 08 09 " +
        "01 02 03 04 05 06 07 08 09 " +
        "01 02 03 04 05 06 07 08 09"
    )

    @Test
    fun `Write and read single block partially`() {
        bytes.write("01 02 03".toByteArray())

        assertEquals("01 02", bytes.readAdvance(2).toHexString(true))
    }

    @Test
    fun `Write and read single block fully`() {
        bytes.write("01 02 03".toByteArray())

        assertEquals("01 02 03", bytes.readAdvance().toHexString(true))
    }

    @Test
    fun `Write and read multiple times`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c".toByteArray())

        assertEquals("01 02 03", bytes.read(3).toHexString(true))
        assertEquals("01 02 03 04 05", bytes.read(5).toHexString(true))

        bytes.advance(2)

        assertEquals("03 04 05 06", bytes.read(4).toHexString(true))
    }

    @Test
    fun `Write and read multi-block partially`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d".toByteArray())

        assertEquals("01 02 03 04 05 06 07 08 09 0a", bytes.readAdvance(10).toHexString(true))
    }

    @Test
    fun `Write and read multi-block block fully`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d".toByteArray())

        assertEquals("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d", bytes.readAdvance().toHexString(true))
    }

    @Test
    fun `Write and read multi-block block progressively`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d".toByteArray())

        assertEquals("01 02 03", bytes.readAdvance(3).toHexString(true))
        assertEquals("04 05", bytes.readAdvance(2).toHexString(true))
        assertEquals("06", bytes.readAdvance(1).toHexString(true))
        assertEquals("07 08 09 0a 0b", bytes.readAdvance(5).toHexString(true))
        assertEquals("0c 0d", bytes.readAdvance(2).toHexString(true))
    }

    @Test
    fun `Write and read across block gaps`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11".toByteArray())

        assertEquals("01 02 03", bytes.readAdvance(3).toHexString(true))
        assertEquals("04 05 06 07 08 09 0a 0b 0c 0d 0e", bytes.readAdvance(11).toHexString(true))
        assertEquals("0f 10 11", bytes.readAdvance(3).toHexString(true))
    }

    @Test
    fun `Copy into buffer skipping within first block`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11".toByteArray())
        assertEquals("04 05 06 07", ByteArray(4).also { bytes.copyTo(it, offset = 3) }.toHexString(true))
    }

    @Test
    fun `Copy into buffer skipping across two blocks`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11".toByteArray())

        assertEquals("08 09 0a 0b 0c 0d 0e", ByteArray(7).also { bytes.copyTo(it, offset = 7) }.toHexString(true))
    }

    @Test
    fun `Copy into buffer skipping across three blocks`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11".toByteArray())

        assertEquals("08 09 0a 0b 0c 0d 0e 0f 10 11", ByteArray(10).also { bytes.copyTo(it, offset = 7) }.toHexString(true))
    }

    @Test
    fun `Write and read with exhaustion`() {
        bytes.write("01 02 03".toByteArray())
        assertEquals(3, bytes.size)

        bytes.write("04 05 06".toByteArray())
        assertEquals(6, bytes.size)

        assertEquals("01 02", bytes.readAdvance(2).toHexString(true))
        assertEquals(4, bytes.size)
        assertEquals("03 04 05 06", bytes.readAdvance().toHexString(true))
        assertEquals(0, bytes.size)

        bytes.write("07 08 09 0a 0b".toByteArray())

        assertEquals("07 08 09", bytes.readAdvance(3).toHexString(true))

        bytes.write("0c 0d 0e 0f".toByteArray())

        assertEquals("0a 0b 0c 0d", bytes.readAdvance(4).toHexString(true))
        assertEquals("0e 0f", bytes.readAdvance(2).toHexString(true))
    }

    @Test
    fun `Read from subarray`() {
        bytes.write("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15".toByteArray())

        val subbytes = bytes.subarray(3, 8)

        assertEquals(8, subbytes.size)
        assertEquals("04 05 06 07 08 09", subbytes.readAdvance(6).toHexString(true))
        assertEquals(2, subbytes.size)
        assertEquals("0a 0b", subbytes.readAdvance(2).toHexString(true))
        assertEquals(0, subbytes.size)
    }
}
