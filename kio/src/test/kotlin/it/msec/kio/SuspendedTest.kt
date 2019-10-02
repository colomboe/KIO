package it.msec.kio

import it.msec.kio.runtime.v2.RuntimeSuspended
import org.junit.Test

class SuspendedTest {

    @Test
    fun suspendedRuntimeTest() {

        val kio = just(33)
                .map { it + 3 }
                .flatMap { i ->
                    ask { r: Int -> r + i }
                }
                .flatMap { i -> unsafeSuspended { println(i) }  }

        println(RuntimeSuspended.unsafeRunSync(kio, 11))
    }
}
