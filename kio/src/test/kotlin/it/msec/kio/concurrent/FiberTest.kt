package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.common.tuple.T
import it.msec.kio.runtime.RuntimeSuspended
import kotlinx.coroutines.delay
import org.junit.Test

class FiberTest {

    @Test
    fun `fibers can be launched`() {

        val first = ask { r: Int ->
            suspendedR<Int, String> {
                println("first start")
                delay(5000)
                println("first end")
                "first $r (${Thread.currentThread().name})"
            }
        }

        val second = ask { r: Int ->
            suspendedR<Int, String> {
                println("second start")
                delay(5000)
                println("second end")
                "second $r (${Thread.currentThread().name})"
            }
        }

        val prog = effect { println("Result:") }
                .flatMap { first.fork() }
                .flatMapT { second.fork() }
                .flatMap { (a, b) -> a.await().flatMap { ra -> b.await().map { rb -> T(ra, rb) } } }
                .flatMap { (a, b) ->
                    effect {
                        println(a)
                        println(b)
                        println("done")
                    }
                }
                .provide(33)

        val result = RuntimeSuspended.unsafeRunSyncAndGet(prog)
        println(result)

    }
}
