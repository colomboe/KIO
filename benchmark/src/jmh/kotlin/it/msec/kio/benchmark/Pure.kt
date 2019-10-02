package it.msec.kio.benchmark

import it.msec.kio.UIO
import it.msec.kio.flatMap
import it.msec.kio.just
import it.msec.kio.runtime.v2.Runtime
import it.msec.kio.runtime.v2.RuntimeSuspended
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class Pure {

    @Param("3000")
    var size: Int = 0

    @Benchmark
    fun kioPure(): Int = Runtime.unsafeRunSyncAndGet(kioPureLoop(size, 0))

    fun kioPureSuspended(): Int = RuntimeSuspended.unsafeRunSyncAndGet(kioPureLoop(size, 0))

    private fun kioPureLoop(size: Int, i: Int): UIO<Int> =
            just(i).flatMap { j ->
                if (j > size) just(j) else kioPureLoop(size, j + 1)
            }

}
