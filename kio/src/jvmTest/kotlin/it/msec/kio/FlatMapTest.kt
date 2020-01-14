package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.runtime.Runtime.unsafeRunSyncAndGet
import org.junit.Test

class FlatMapTest {

    @Test
    fun `nested flatmap and lazy constructors`() {

        fun loop(n: Int): UIO<Int> =
                if (n <= 1) effect { n }
                else loop(n - 1).flatMap { a ->
                    loop(n - 2).flatMap { b -> effect { a + b } }
                }

        val kio = loop(20)
        val value = unsafeRunSyncAndGet(kio)
        assertThat(value).isEqualTo(6765)
    }

    @Test
    fun `flatMap is stack safe`() {
        val iterations = 100000

        var kio = effect { 33 }
        for (i in 1..iterations) kio = kio.flatMap { effect { it + 1 } }

        val result = kio.unsafeRunSyncAndGet()
        assertThat(result).isEqualTo(33 + iterations)
    }

}
