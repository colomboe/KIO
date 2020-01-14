package it.msec.kio

import it.msec.kio.result.get
import it.msec.kio.runtime.CoroutineInterpreter.unsafeRunSuspended
import it.msec.kio.runtime.Runtime
import it.msec.kio.runtime.RuntimeSuspended
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

@Ignore
class CoroutinePerformancesTest {

    @Test
    fun `performance comparison when inside coroutine`() = runBlocking {

        val program = loop(32)

        for (i in 1..5) Runtime.unsafeRunSyncAndGet(program)
        for (i in 1..5) unsafeRunSuspended(program, Unit).get()

        val (_, coroutineMillis) = runSuspendedAndGetTimeMillis { unsafeRunSuspended(program, Unit).get() }
        println(coroutineMillis)

        val (_, noCoroutineMillis) = runAndGetTimeMillis { Runtime.unsafeRunSyncAndGet(program) }
        println(noCoroutineMillis)
    }

    @Test
    fun `performance comparison when outside coroutine`() {

        val program = loop(32)

        for (i in 1..5) Runtime.unsafeRunSyncAndGet(program)
        for (i in 1..5) RuntimeSuspended.unsafeRunSyncAndGet(program)

        val (_, coroutineMillis) = runAndGetTimeMillis { RuntimeSuspended.unsafeRunSyncAndGet(program) }
        println(coroutineMillis)

        val (_, noCoroutineMillis) = runAndGetTimeMillis { Runtime.unsafeRunSyncAndGet(program) }
        println(noCoroutineMillis)
    }


    private fun loop(n: Int): UIO<Int> =
            if (n <= 1) effect { n }
            else loop(n - 1).flatMap { a ->
                loop(n - 2).flatMap { b -> effect { a + b } }
            }
}
