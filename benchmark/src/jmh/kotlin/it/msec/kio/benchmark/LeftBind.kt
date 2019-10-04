package it.msec.kio.benchmark

import it.msec.kio.UIO
import it.msec.kio.flatMap
import it.msec.kio.just
import it.msec.kio.runtime.Runtime.unsafeRunSyncAndGet
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class LeftBind {

    @Param("10000")
    var size: Int = 0

    @Param("100")
    var depth: Int = 0

    @Benchmark
    fun kio(): Int =
            unsafeRunSyncAndGet(loop(depth, size, 0))

    private fun loop(depth: Int, size: Int, i: Int): UIO<Int> =
            when {
                i % depth == 0 -> just(i + 1).flatMap { loop(depth, size, it) }
                i < size -> loop(depth, size, i + 1).flatMap { just(it) }
                else -> just(i)
            }


}
