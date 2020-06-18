package dev.mpr.grpc

import dev.mpr.grpc.protobuf.tools.ProtobufOutputStream
import dev.mpr.grpc.protobuf.tools.ProtobufWriter
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtobufOuputStreamTests {

    lateinit var stream: ProtobufOutputStream

    @BeforeTest
    fun setup() {
        stream = ProtobufOutputStream(8)
    }

    private fun verifyWrite(data: String, writer: ProtobufWriter.() -> Unit) {
        stream.write(writer)

        assertEquals(data, stream.toByteArray().toHexString(true))
    }

    @Test
    fun `Write basic message`() {
        verifyWrite("08 96 01") {
            // message Test1 {
            //     optional int32 a = 1;
            // }
            encode(1, 150)
        }
    }

    @Test
    fun `Embedded message`() = verifyWrite("1a 03 08 96 01") {
        // message Test1 {
        //     optional int32 a = 1;
        // }
        //
        // message Test3 {
        //     optional Test1 c = 3;
        // }
        encode(3) {
            encode(1, 150)
        }
    }

    @Test
    fun `Deeper embedding`() = verifyWrite("0a 07 12 05 1a 03 20 96 01") {
        // message Test1 {
        //     optional Test2 a = 1;
        // }
        //
        // message Test2 {
        //     optional Test3 b = 2;
        // }
        //
        // message Test3 {
        //     optional Test4 c = 3;
        // }
        //
        // message Test4 {
        //     optional int32 d = 4;
        // }
        encode(1) {
            encode(2) {
                encode(3) {
                    encode(4, 150)
                }
            }
        }
    }

    @Test
    fun `Short embeded message`() = verifyWrite(
        "0a 09 12 07 " +
        "00 01 02 03 04 05 06") {
        encode(1) {
            encode(2, "00 01 02 03 04 05 06".toByteArray())
        }
    }

    @Test
    fun `Long embeded message`() = verifyWrite(
        "0a c3 01 12 c0 01 " +
        "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
        "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
        "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
        "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f " +
        "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
        "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
        "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
        "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f " +
        "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
        "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
        "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
        "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f") {
        // message Test1 {
        //     optional Test2 a = 1;
        // }
        //
        // message Test2 {
        //     optional string b = 2;
        // }
        encode(1) {
            encode(2, (
                    "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
                    "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
                    "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
                    "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f " +
                    "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
                    "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
                    "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
                    "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f " +
                    "00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f " +
                    "10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f " +
                    "20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
                    "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f"
                ).toByteArray()
            )
        }
    }

    @Test
    fun `Write variable length bytes`() = verifyWrite("12 07 74 65 73 74 69 6e 67") {
        // message Test2 {
        //     optional string b = 2;
        // }
        encode(2, "74 65 73 74 69 6e 67".toByteArray())
    }
}
