package sample

import dev.mpr.grpc.sample.Empty
import dev.mpr.grpc.sample.Nested
import dev.mpr.grpc.sample.FullOfScalars

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    val name: String
}

fun hello(): String {
    FullOfScalars.build {
        aBool = true
        anInt32 = 12
        aUint64 = Int.MAX_VALUE.toULong() + 2UL
    }

    Empty()

    Nested.build {
        outerInt = 1
        innerMessage {
            innerInt = 2
            nestedInnerMessage {
                innerInnerInt = 3
            }
        }
    }

    return "Hello from Test"
}

class Proxy {
    fun proxyHello() = hello()
}

fun main() {
    println(hello())
}