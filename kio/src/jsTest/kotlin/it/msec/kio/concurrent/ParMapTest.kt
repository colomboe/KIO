package it.msec.kio.concurrent

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.common.tuple.T
import it.msec.kio.recover
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunPromiseAndGet
import it.msec.kio.suspended
import kotlinx.coroutines.delay
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.test.Test

class ParMapTest {

    private fun <A> testEffect(ms: Long, value: A) = suspended {
        delay(ms)
        value
    }

    @Test
    fun `parMapN_maps_multiple_effects_in_parallel`(): Promise<Unit> {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, 20)
        val e3 = testEffect(500, 30)

        val out = parMapN(e1, e2, e3) { rs ->
            rs.reduce { a, b -> a + b }
        }.recover { 0 }

        return runMeasureAndAssert(
                { unsafeRunPromiseAndGet(out) },
                { result, millis ->
                    assertThat(result).isEqualTo(60)
                    assertThat(millis).isBetween(500, 800)
                }
        )
    }

    @Test
    fun `parMap_maps_2_effects_in_parallel`(): Promise<Unit> {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")

        val out = parMapN(e1, e2) { a, b -> "$b $a" }.recover { "" }

        return runMeasureAndAssert(
                { unsafeRunPromiseAndGet(out) },
                { result, millis ->
                    assertThat(result).isEqualTo("Hello 10")
                    assertThat(millis).isBetween(500, 800)
                }
        )
    }

    @Test
    fun `parMap_maps_3_effects_in_parallel`(): Promise<Unit> {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")
        val e3 = testEffect(500, T("be", "box"))

        val out = parMapN(e1, e2, e3) { a, b, c ->
            "$b $a (${c._1} ${c._2})"
        }.recover { "" }

        return runMeasureAndAssert(
                { unsafeRunPromiseAndGet(out) },
                { result, millis ->
                    assertThat(result).isEqualTo("Hello 10 (be box)")
                    assertThat(millis).isBetween(500, 700)
                }
        )
    }

    inline fun <A> runMeasureAndAssert(crossinline f: () -> Promise<A>, crossinline assertF: (A, Long) -> Unit): Promise<Unit> {
        val start = Date().getTime().toLong()
        return f().then {
            val end = Date().getTime().toLong()
            assertF(it, end - start)
        }
    }

}
