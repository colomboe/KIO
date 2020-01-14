package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunPromiseAndGet
import kotlin.test.Test

class FlatMapTest {

    @Test
    fun nested_flatMap_and_lazy_constructors() {

        fun loop(n: Int): UIO<Int> =
                if (n <= 1) effect { n }
                else loop(n - 1).flatMap { a ->
                    loop(n - 2).flatMap { b -> effect { a + b } }
                }

        val kio = loop(20)
        val promise = unsafeRunPromiseAndGet(kio)
        promise.then { v -> assertThat(v).isEqualTo(6765) }
    }

    @Test
    fun flatMap_is_stack_safe() {
        val iterations = 100000

        var kio = effect { 33 }
        for (i in 1..iterations) kio = kio.flatMap { effect { it + 1 } }

        val promise = unsafeRunPromiseAndGet(kio)
        promise.then { v -> assertThat(v).isEqualTo(33 + iterations) }
    }

}
