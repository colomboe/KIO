package it.msec.kio.kio

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.*
import it.msec.kio.ng.unsafeRunSync
import it.msec.kio.utils.list.sequence
import org.junit.Test

class SequenceTest {

    @Test
    fun sequenceOk() {
        val xs = listOf(just(11), just(12), just(13))
        val kio = xs.sequence()
        assertThat(kio.unsafeRunSync().get()).containsExactly(11, 12, 13)
    }

    @Test
    fun sequenceError() {
        val xs = listOf(just(11), just(12), failure("error"))
        val kio = xs.sequence()
        val result = kio.unsafeRunSync()
        assertThat(result)
                .isInstanceOf(Ko::class)
                .transform { it.error }
                .isEqualTo("error")
    }

    @Test
    fun sequenceEnvOk() {
        val env = "StringEnv"
        val xs: List<EnvTask<String, Int>> = listOf(justEnv(11), justEnv(12), justEnv(13))
        val kio = xs.sequence()
        assertThat(kio.unsafeRunSync(env).get()).containsExactly(11, 12, 13)
    }
}
