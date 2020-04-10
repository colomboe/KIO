package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.common.tuple.T2
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
                .flatMap { (a, b) -> mapN(a.await(), b.await(), ::T2) }
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

        val prog =
            effect { println("Result:") } +
            first.fork()    to { fiber1 ->
            second.fork()   to { fiber2 ->
            fiber1.await()  to { r1 ->
            fiber2.await()  to { r2 ->
            effect {
                println(r1)
                println(r2)
                println("done")
            }
        }}}}

        val result = RuntimeSuspended.unsafeRunSync(prog, 33).get()
        println(result)
    }
}
