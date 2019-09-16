package it.msec.kio

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.common.tuple.T
import it.msec.kio.runtime.unsafeRunSyncAndGet
import kotlinx.coroutines.delay
import org.junit.Test

class ParMapTest {

    private fun <A> testEffect(ms: Long, value: A) = unsafe {
        delay(ms)
        value
    }

    @Test
    fun parMapNTest() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, 20)
        val e3 = testEffect(500, 30)

        val out = parMapN(e1, e2, e3) { rs ->
            rs.reduce { a, b -> a + b }
        }.recover { 0 }

        val start = System.currentTimeMillis();
        val result = out.unsafeRunSyncAndGet()
        val stop = System.currentTimeMillis();

        assertThat(result).isEqualTo(60)
        assertThat(stop - start).isBetween(500, 700)
    }

    @Test
    fun parMap2Test() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")

        val out = parMap(e1, e2) { a, b -> "$b $a" }.recover { "" }

        val start = System.currentTimeMillis();
        val result = out.unsafeRunSyncAndGet()
        val stop = System.currentTimeMillis();

        assertThat(result).isEqualTo("Hello 10")
        assertThat(stop - start).isBetween(500, 700)
    }

    @Test
    fun parMap3Test() {

        val e1 = testEffect(500, 10)
        val e2 = testEffect(500, "Hello")
        val e3 = testEffect(500, T("be", "box"))

        val out = parMap(e1, e2, e3) { a, b, c ->
            "$b $a (${c._1} ${c._2})"
        }.recover { "" }

        val start = System.currentTimeMillis();
        val result = out.unsafeRunSyncAndGet()
        val stop = System.currentTimeMillis();

        assertThat(result).isEqualTo("Hello 10 (be box)")
        assertThat(stop - start).isBetween(500, 700)
    }
}
