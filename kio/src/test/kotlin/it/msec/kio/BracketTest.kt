package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.result.Failure
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSyncAndGet
import org.junit.Test

class BracketTest {

    @Test
    fun `everything successful`() {

        var sideEffect = 0
        val acquisition = effect { 33 }
        val processing = { i: Int -> effect { i + 11 } }
        val release = { i: Int -> effect { sideEffect = i + 1 } }

        val kio = acquisition.bracket(release, processing)
        val result = unsafeRunSyncAndGet(kio)

        assertThat(sideEffect).isEqualTo(34)
        assertThat(result).isEqualTo(44)
    }

    @Test
    fun `processing failed`() {

        var sideEffect = 0
        val acquisition = effect { 33 }
        val processing = { _: Int -> failure("Error") }
        val release = { i: Int -> effect { sideEffect = i + 1 } }

        val kio = acquisition.bracket(release, processing)
        val result = unsafeRunSync(kio)

        assertThat(sideEffect).isEqualTo(34)
        assertThat(result)
                .isInstanceOf(Failure::class)
                .transform { it.error }
                .isEqualTo("Error")
    }

    @Test
    fun `acquisition failed`() {

        var sideEffect = 0
        val acquisition = failure("Failed acquisition")
        val processing = { i: Int -> effect { i + 11 } }
        val release = { i: Int -> effect { sideEffect = i + 1 } }

        val kio = acquisition.bracket(release, processing)
        val result = unsafeRunSync(kio)

        assertThat(sideEffect).isEqualTo(0)

        assertThat(result)
                .isInstanceOf(Failure::class)
                .transform { it.error }
                .isEqualTo("Failed acquisition")
    }
}
