package it.msec.kio.benchmark

import kio.*
import kio.result.getOrThrow
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class AttemptNonRaised {

    @Param("10000")
    var size: Int = 0

    @Benchmark
    fun kio(): Int = kio.runtime.Runtime.unsafeRunSync(loopHappy(size, 0)).getOrThrow()

    fun loopHappy(size: Int, i: Int): Task<Int> =
            if (i < size) {
                effect { i + 1 }
                        .attempt()
                        .recover { throw RuntimeException("not happening") }
                        .flatMap { loopHappy(size, it) }
            } else
                just(1)

}
