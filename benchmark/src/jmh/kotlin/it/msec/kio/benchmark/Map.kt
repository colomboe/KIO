package it.msec.kio.benchmark

import it.msec.kio.just
import it.msec.kio.map
import it.msec.kio.runtime.KIORuntime
import it.msec.kio.runtime.v2.Runtime
import it.msec.kio.runtime.v2.RuntimeSuspended
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class Map {

    @Benchmark
    fun kioOne(): Long = Runtime.kioMapTest(12000, 1)

    @Benchmark
    fun kioBatch30(): Long = Runtime.kioMapTest(12000 / 30, 30)

    @Benchmark
    fun kioBatch120(): Long = Runtime.kioMapTest(12000 / 120, 120)

    @Benchmark
    fun kioOneSuspended(): Long = RuntimeSuspended.kioMapTest(12000, 1)

    @Benchmark
    fun kioBatch30Suspended(): Long = RuntimeSuspended.kioMapTest(12000 / 30, 30)

    @Benchmark
    fun kioBatch120Suspended(): Long = RuntimeSuspended.kioMapTest(12000 / 120, 120)

    private fun KIORuntime.kioMapTest(iterations: Int, batch: Int): Long {
        val f = { x: Int -> x + 1 }
        var fx = just(0)

        var j = 0
        while (j < batch) {
            fx = fx.map(f)
            j += 1
        }

        var sum = 0L
        var i = 0
        while (i < iterations) {
            sum += unsafeRunSyncAndGet(fx)
            i += 1
        }
        return sum
    }

}
