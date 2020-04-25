package it.msec.kio

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.concurrent.parMapN
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

    private val twoTimes =
            parMapN(cpuIntensiveEffect, cpuIntensiveEffect) { x, _ -> x }.recover { 0 }

    @Test
    fun `single-thread context`() {

        val singleThreadCtx = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(twoTimes, singleThreadCtx) }

        assertThat(result).isEqualTo(33)
        assertThat(millis).isBetween(1000, 1400)
    }

    @Test
    fun `multi-thread context`() {

        val multiThreadCtx = Executors.newFixedThreadPool(3).asCoroutineDispatcher()
        val (result, millis) = runAndGetTimeMillis { unsafeRunSyncAndGet(twoTimes, multiThreadCtx) }

        assertThat(result).isEqualTo(33)
        assertThat(millis).isBetween(500, 1000)

    }
}
