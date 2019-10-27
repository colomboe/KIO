package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSyncAndGet
import org.junit.Test

class ComprehensionTest {

    @Test
    fun `basic comprehension works`() {

//        val kio: UIO<String> = binding {
//            val x by +effect { 33 }
//            val y by +effect { 2 * x }
//            val s1 by +just("Hello")
//            val s2 by +effect { "$s1 world!" }
//            "$s2 ${x + y}"
//        }

        val kio =
            effect { 33 }             to { x ->
            effect { 2 * x }          to { y ->
            just("Hello")          to { s1 ->
            effect { "$s1 world!" }   map { s2 ->
            "$s2 ${x + y}"
        }}}}

        val total = unsafeRunSyncAndGet(kio)
        assertThat(total).isEqualTo("Hello world! 99")
    }

    @Test
    fun `basic comprehension with error`() {

        fun shouldFail(): IO<TestError, String> = failure(BigError)

//        val kio: IO<TestError, String> = binding {
//            val s1 by +just("Hello")
//            val s2 by +shouldFail()
//            val s3 by +effect { "$s1 $s2 world!" }
//            s3
//        }

        val kio =
            just("Hello")               to { s1 ->
            shouldFail()                   to { s2 ->
            effect { "$s1 $s2 world!" }
        }}

        val result = unsafeRunSync(kio)
        assertThat(result)
                .isInstanceOf(Failure::class)
                .transform { it.error }
                .isEqualTo(BigError)
    }

    @Test
    fun `comprehension works with environment`() {

        fun retrieveValue(): KIO<String, TestError, Int> = ask { unsafe { it.toInt() }.mapError { BigError } }
        fun incrementValue(x: Int): UIO<Int> =  effect { x + 1 }

//        val kio: KIO<String, TestError, Int> = binding {
//            val start by +retrieveValue()
//            val inc by +incrementValue(start)
//            inc
//        }

        val kio =
            retrieveValue() to { start ->
            incrementValue(start)
        }

        val result = unsafeRunSync(kio, "33")
        assertThat(result).isInstanceOf(Success::class).transform { it.value }.isEqualTo(34)

        val result2 = unsafeRunSync(kio, "uu")
        assertThat(result2).isInstanceOf(Failure::class).transform { it.error }.isEqualTo(BigError)
    }

    @Test
    fun `comprehension is stack safe`() {

//        fun recursive(i: Int, max: Int): UIO<Int> = binding {
//            val current = i + 1
//            if (current == max)
//                current
//            else {
//                val u by +recursive(current, max)
//                u
//            }
//        }

        tailrec fun recursive(i: Int, max: Int): UIO<Int> {
            val current = i + 1
            return if (current == max)
                just(current)
            else
                recursive(current, max)
        }

        val iterations = 100000
        val kio = recursive(0, iterations)
        val result = unsafeRunSyncAndGet(kio)
        assertThat(result).isEqualTo(iterations)
    }
}

sealed class TestError
object BigError : TestError()
