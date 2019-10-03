package it.msec.kio.benchmark

import it.msec.kio.UIO
import it.msec.kio.effect
import it.msec.kio.flatMap
import it.msec.kio.runtime.Runtime
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class Delay {

    @Param("3000")
    var size: Int = 0

    private fun kioDelayLoop(size: Int, i: Int): UIO<Int> =
            effect { i }.flatMap { j ->
                if (j > size) effect { j } else kioDelayLoop(size, j + 1)
            }

    @Benchmark
    fun kio(): Int = Runtime.unsafeRunSyncAndGet(kioDelayLoop(size, 0))

}
