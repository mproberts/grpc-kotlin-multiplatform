package dev.mpr.grpc

import com.google.protobuf.NullValue
import com.google.protobuf.Struct
import com.google.protobuf.Timestamp
import com.google.protobuf.Value
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
    fun `Build bigger type`() {
        val struct = Struct.build {
            fields {
                value("test1") {
                    kind {
                        boolValue = true
                    }
                }
                value("test2") {
                    kind {
                        numberValue = 42.0
                    }
                }
                value("test3") {
                    kind {
                        listValue {
                            values {
                                addValue {
                                    kind {
                                        nullValue = NullValue.NULL_VALUE
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        assertEquals(true, (struct.fields["test1"]?.kind as? Value.OneOfKind.boolValue)?.boolValue)
        assertEquals(42.0, (struct.fields["test2"]?.kind as? Value.OneOfKind.numberValue)?.numberValue)
        assertEquals(NullValue.NULL_VALUE, ((struct.fields["test3"]?.kind as? Value.OneOfKind.listValue)?.listValue?.values?.getOrNull(0)?.kind as? Value.OneOfKind.nullValue)?.nullValue)
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
