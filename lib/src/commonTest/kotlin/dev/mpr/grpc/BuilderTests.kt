package dev.mpr.grpc

import com.google.protobuf.Timestamp
import kotlin.test.Test
import kotlin.test.assertEquals

class BuilderTests {
    @Test
    fun `Build standard types`() {
        val timestamp = Timestamp.build {
            seconds = 1310090400
        }

        assertEquals(0, timestamp.nanos)
        assertEquals(1310090400, timestamp.seconds)
    }

    @Test
    fun `Modifying built object`() {
        val timestamp = Timestamp.build {
            seconds = 1310090400
        }

        val updatedTimestamp = timestamp.copyBuild {
            nanos = 1
        }

        assertEquals(0, timestamp.nanos)
        assertEquals(1310090400, timestamp.seconds)

        assertEquals(1, updatedTimestamp.nanos)
        assertEquals(1310090400, updatedTimestamp.seconds)
    }
}
