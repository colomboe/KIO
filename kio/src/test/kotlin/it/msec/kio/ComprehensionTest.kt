package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.result.Failure
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSyncAndGet
import org.junit.Test

class ComprehensionTest {

    @Test
    fun `basic comprehension works`() {

        val kio: UIO<String> = binding {
            val x by +effect { 33 }
            val y by +effect { 2 * x }
            val s1 by +just("Hello")
            val s2 by +effect { "$s1 world!" }
            "$s2 ${x + y}"
        }

        val total = unsafeRunSyncAndGet(kio)
        assertThat(total).isEqualTo("Hello world! 99")
    }

    @Test
    fun `basic comprehension with error`() {

        val kio: IO<TestError, String> = binding {
            val s1 by +just("Hello")
            val s2 by +failure(BigError)
            val s3 by +effect { "$1 world!" }
            s3
        }

        val result = unsafeRunSync(kio)
        assertThat(result)
                .isInstanceOf(Failure::class)
                .transform { it.error }
                .isEqualTo(BigError)
    }

    @Test
    fun `stack safety test`() {
//        TODO()
    }
}

sealed class TestError()
object BigError : TestError()
object LittleError : TestError()
