package it.msec.kio.benchmark

import it.msec.kio.*
import it.msec.kio.runtime.Runtime.unsafeRunSync
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10)
@CompilerControl(CompilerControl.Mode.DONT_INLINE)
open class AttemptRaisedError {

    @Param("10000")
    var size: Int = 0

    @Benchmark
    fun attemptRaisedError() =
            unsafeRunSync(loopNotHappy(size, 0))

    private fun loopNotHappy(size: Int, i: Int): Task<Int> =
            if (i < size) {
                effect { throw dummy }
                        .attempt()
                        .tryRecover { loopNotHappy(size, i + 1) }
            } else
                just(1)

    private val dummy = object : RuntimeException("dummy") {
        override fun fillInStackTrace(): Throwable = this
    }
}
