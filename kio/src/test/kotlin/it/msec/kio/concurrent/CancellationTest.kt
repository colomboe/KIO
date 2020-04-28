package it.msec.kio.concurrent

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.*
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import org.junit.Test
import java.util.concurrent.Executors

class CancellationTest {

    object MyResource
    object MySecondResource

    @Test
    fun `cancellation with bracket`() {

        var releaseCounter = 0

        val fiber = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .bracket({ doPrint("Releasing resource").flatMap { effect { releaseCounter++ } } },
                         { suspended { delay(5000) }.map { "done" } })
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }
                .fork()

        val prog = fiber
                .peek { doPrint("Forked") }
                .peek { suspended { delay(1000) } }
                .peek { doPrint("Cancelling") }
                .flatMap { it.cancel() }
                .flatMap { doPrint("Cancelled") }
                .flatMap { suspended { delay(1000) } }
                .flatMap { doPrint("End") }

        unsafeRunSync(prog)

        assertThat(releaseCounter).isEqualTo(1)
    }

    @Test
    fun `cancellation with race`() {

        var releaseCounter = 0

        fun fiberWithDelay(ms: Long) = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .bracket({ doPrint("Releasing resource").flatMap { effect { releaseCounter++ } } },
                         { suspended { delay(ms) }.map { "done" } })
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }

        val prog = race(fiberWithDelay(5000), fiberWithDelay(1000), ::just, ::just)

        unsafeRunSync(prog)

        assertThat(releaseCounter).isEqualTo(2)
    }
    @Test
    fun `cancellation with ensuring`() {

        var releaseCounter = 0

        val fiber = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .peek { suspended { delay(5000) }.map { "done" } }
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }
                .ensuring { doPrint("Releasing resource").flatMap { effect { releaseCounter++ } } }
                .fork()

        val prog = fiber
                .peek { doPrint("Forked") }
                .peek { suspended { delay(1000) } }
                .peek { doPrint("Cancelling") }
                .flatMap { it.cancel() }
                .flatMap { doPrint("Cancelled") }
                .flatMap { suspended { delay(1000) } }
                .flatMap { doPrint("End") }

        unsafeRunSync(prog)

        assertThat(releaseCounter).isEqualTo(1)
    }

    @Test
    fun `cancellation with nested bracket`() {

        var releaseCounter = 0

        val subjob = doPrint("Starting sub-job")
                .flatMap { doPrint("Acquiring resource 2").map { MySecondResource }.peek { doPrint("Acquired 2") } }
                .bracket({ r -> doPrint("Releasing resource 2 ($r)").flatMap { effect { releaseCounter++ } } },
                        { suspended { delay(5000) }.map { "done 2" } })
                .flatMap { doPrint("Do other things 2") }
                .peekError { doPrint("Don't do things") }

        val fiber = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .bracket({ r -> doPrint("Releasing resource ($r)").flatMap { effect { releaseCounter++ } } }, { subjob })
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }
                .fork()

        val prog = fiber
                .peek { doPrint("Forked") }
                .peek { suspended { delay(1000) } }
                .peek { doPrint("Cancelling") }
                .flatMap { it.cancel() }
                .flatMap { doPrint("Cancelled") }
                .flatMap { suspended { delay(1000) } }
                .flatMap { doPrint("End") }

        unsafeRunSync(prog)

        assertThat(releaseCounter).isEqualTo(2)
    }

    @Test
    fun `cancellation with task without suspend points`() {

        val context = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

        var releaseCounter = 0

        fun highCPUJob(size: Int, i: Int = 0): UIO<Int> =
                effect { Thread.sleep(100); i }.flatMap { j ->
                    if (j > size) effect { j } else highCPUJob(size, j + 1)
                }

        val fiber = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .bracket({ r -> doPrint("Releasing resource ($r)").flatMap { effect { releaseCounter++ } } }, { highCPUJob(50) })
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }
                .fork()

        val prog = fiber
                .peek { doPrint("Forked") }
                .peek { suspended { delay(1000) } }
                .peek { doPrint("Cancelling") }
                .flatMap { it.cancel() }
                .peek { doPrint("Cancelled") }

        val (_, millis) = runAndGetTimeMillis { unsafeRunSync(prog, context) }
        assertThat(releaseCounter).isEqualTo(1)
        assertThat(millis).isBetween(0, 2000)
    }

    @Test
    fun `cancellation with task without suspend points (but with suspend effects)`() {

        val context = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

        var releaseCounter = 0

        fun highCPUJob(size: Int, i: Int = 0): UIO<Int> =
                suspended { Thread.sleep(100); i }.flatMap { j ->
                    if (j > size) suspended { j } else highCPUJob(size, j + 1)
                }

        val fiber = doPrint("Starting Fiber")
                .flatMap { doPrint("Acquiring resource").map { MyResource }.peek { doPrint("Acquired") } }
                .bracket({ r -> doPrint("Releasing resource ($r)").flatMap { suspended { delay(100); releaseCounter++ } } }, { highCPUJob(50) })
                .flatMap { doPrint("Do other things") }
                .peekError { doPrint("Don't do things") }
                .fork()

        val prog = fiber
                .peek { doPrint("Forked") }
                .peek { suspended { delay(1000) } }
                .peek { doPrint("Cancelling") }
                .flatMap { it.cancel() }
                .peek { doPrint("Cancelled") }

        val (_, millis) = runAndGetTimeMillis { unsafeRunSync(prog, context) }
        assertThat(releaseCounter).isEqualTo(1)
        assertThat(millis).isBetween(0, 2000)
    }

    private fun doPrint(s: String) = effect { println("${Thread.currentThread().name} $s") }

}
