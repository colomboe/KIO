package it.msec.kio

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSyncAndGet
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.Test
import java.util.concurrent.Executors

class CoroutineContextTest {

    @Suppress("BlockingMethodInNonBlockingContext")
    private val cpuIntensiveEffect = suspended {
        Thread.sleep(500)
        33
    }

    private val threeTimes =
            parMapN(cpuIntensiveEffect, cpuIntensiveEffect, cpuIntensiveEffect) { x, _, _ -> x }.recover { 0 }

    @Test
    fun `single-thread context`() {

        val singleThreadCtx = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(threeTimes, singleThreadCtx) }

        assertThat(result).isEqualTo(33)
        assertThat(millis).isBetween(1500, 2000)
    }

    @Test
    fun `multi-thread context`() {

        val multiThreadCtx = Executors.newFixedThreadPool(3).asCoroutineDispatcher()
        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(threeTimes, multiThreadCtx) }

        assertThat(result).isEqualTo(33)
        assertThat(millis).isBetween(500, 1000)

    }
}
