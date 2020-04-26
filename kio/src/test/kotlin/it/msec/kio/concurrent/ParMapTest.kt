package it.msec.kio.concurrent

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.*
import it.msec.kio.common.tuple.T
import it.msec.kio.result.Failure
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSyncAndGet
import kotlinx.coroutines.delay
import org.junit.Test

class ParMapTest {

    private fun <A> testEffect(ms: Long, value: A) = suspended {
        delay(ms)
        value
    }

    private fun <A, E> failingTestEffect(ms: Long, error: E): IO<E, A> = suspended { delay(ms) }.flatMap { failure(error) }

    @Test
    fun `parMapN maps multiple effects in parallel`() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, 20)
        val e3 = testEffect(500, 30)

        val out = parMapN(e1, e2, e3) { rs ->
            rs.reduce { a, b -> a + b }
        }.recover { 0 }

        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(out) }

        assertThat(result).isEqualTo(60)
        assertThat(millis).isBetween(500, 700)
    }

    @Test
    fun `parMap maps 2 effects in parallel`() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")

        val out = parMapN(e1, e2) { a, b -> "$b $a" }.recover { "" }

        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(out) }

        assertThat(result).isEqualTo("Hello 10")
        assertThat(millis).isBetween(500, 800)
    }

    @Test
    fun `parMap maps 2 effects in parallel with failure`() {

        val e1: IO<String, Int> = testEffect(500, 10)
        val e2: IO<String, Int> = failingTestEffect(1000, "Hello")

        val out = parMapN(e1, e2) { a, b -> "$b $a" }

        val (result, millis) = runAndGetTimeMillis { unsafeRunSync(out) }

        assertThat((result as Failure).error).isEqualTo("Hello")
        assertThat(millis).isBetween(1000, 1500)
    }

    @Test
    fun `parMap maps 3 effects in parallel`() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")
        val e3 = testEffect(500, T("be", "box"))

        val out = parMapN(e1, e2, e3) { a, b, c ->
            "$b $a (${c._1} ${c._2})"
        }.recover { "" }

        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(out) }

        assertThat(result).isEqualTo("Hello 10 (be box)")
        assertThat(millis).isBetween(500, 700)
    }
}
