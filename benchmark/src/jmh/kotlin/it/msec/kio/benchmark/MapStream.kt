package it.msec.kio.benchmark

import it.msec.kio.*
import it.msec.kio.result.getOrThrow
import it.msec.kio.runtime.Runtime
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class MapStream {

    @Benchmark
    fun kioOne(): Long =
            test(12000, 1)

    @Benchmark
    fun kioBatch30(): Long =
            test(12000 / 30, 30)

    @Benchmark
    fun kioBatch120(): Long =
            test(12000 / 120, 120)

    fun test(times: Int, batchSize: Int): Long {
        var stream = range(0, times)
        var i = 0
        while (i < batchSize) {
            stream = mapStream(addOne)(stream)
            i += 1
        }

        return Runtime.unsafeRunSync(sum(0)(stream)).getOrThrow()
    }

    data class Stream(val value: Int, val next: Task<Stream?>)

    val addOne = { x: Int -> x + 1 }

    fun range(from: Int, until: Int): Stream? =
            if (from < until)
                Stream(from, effect { range(from + 1, until) })
            else
                null

    fun mapStream(f: (Int) -> Int): (box: Stream?) -> Stream? = { box ->
        if (box != null)
            Stream(f(box.value), box.next.map(mapStream(f)))
        else
            null
    }

    fun sum(acc: Long): (Stream?) -> Task<Long> = { box ->
        box?.next?.flatMap(sum(acc + box.value)) ?: just(acc)
    }

}
