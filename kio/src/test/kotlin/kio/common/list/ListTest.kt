package kio.common.list

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kio.*
import kio.result.Failure
import kio.result.get
import org.junit.Test

class ListTest {

    @Test
    fun `sequence when all values are success`() {
        val xs = listOf(just(11), just(12), just(13))
        val kio = xs.sequence()
        println(kio.unsafeRunSyncAndGet())
        assertThat(kio.unsafeRunSyncAndGet()).containsExactly(11, 12, 13)
    }

    @Test
    fun `sequence when one value is a failure`() {
        val xs = listOf(just(11), just(12), failure("error"))
        val kio = xs.sequence()
        val result = kio.unsafeRunSync()
        assertThat(result)
                .isInstanceOf(Failure::class)
                .transform { it.error }
                .isEqualTo("error")
    }

    @Test
    fun `sequence with environment injection`() {
        val env = "StringEnv"
        val xs: List<URIO<String, Int>> = listOf(justR(11), justR(12), justR(13))
        val kio = xs.sequence()
        assertThat(kio.unsafeRunSync(env).get()).containsExactly(11, 12, 13)
    }
}
