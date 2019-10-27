package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.common.tuple.T
import it.msec.kio.result.get
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

    @Test
    fun `fibers can be launched (with comprehension)`() {

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

        val prog: URIO<Int, Unit> = binding {
            effect { println("Result:") }.bind()
            val fiber1 by +first.fork()
            val fiber2 by +second.fork()
            val r1 by +fiber1.await()
            val r2 by +fiber2.await()

            effect {
                println(r1)
                println(r2)
                println("done")
            }.bind()
        }

        val result = RuntimeSuspended.unsafeRunSync(prog, 33).get()
        println(result)
    }
}
