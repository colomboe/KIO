package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.junit.Test

class CoroutineTimeoutTest {

    @Test
    fun `a suspended effect can end before the timeout`() {

        val f = unsafeSuspended {
            withTimeout(500) {
                delay(100)
                33
            }
        }

        val result = unsafeRunSync(f)
        assertThat(result).isInstanceOf(Success::class).transform { it.value }.isEqualTo(33)
    }

    @Test
    fun `a suspended effect can end after the timeout`() {

        val f = unsafeSuspended {
            withTimeout(500) {
                delay(1000)
                33
            }
        }

        val result = unsafeRunSync(f)
        assertThat(result).isInstanceOf(Failure::class)
        assertThat((result as Failure).error).isInstanceOf(TimeoutCancellationException::class)
    }
}
