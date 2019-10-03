package kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import kio.runtime.Runtime.unsafeRunSyncAndGet
import org.junit.Test

class FlatMapTest {

    @Test
    fun `nested flatmap and lazy constructors`() {

        fun loop(n: Int): UIO<Int> =
                if (n <= 1) effect { n }
                else loop(n - 1).flatMap { a ->
                    loop(n - 2).flatMap { b -> effect { a + b } }
                }

        val io = loop(20)
        val x = unsafeRunSyncAndGet(io)
        assertThat(x).isEqualTo(6765)
    }

}
