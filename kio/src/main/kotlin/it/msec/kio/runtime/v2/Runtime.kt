package it.msec.kio.runtime.v2

import it.msec.kio.*
import it.msec.kio.result.Result
import it.msec.kio.result.get
import it.msec.kio.runtime.KIORuntime
import it.msec.kio.runtime.RuntimeFn
import it.msec.kio.runtime.RuntimeStack

object Runtime : KIORuntime {

    override fun <A> unsafeRunSyncAndGet(kio: UIO<A>) =
            execute(kio, Unit).get()

    override fun <E, A> unsafeRunSync(kio: IO<E, A>) =
            execute(kio, Unit)

    override fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R) =
            execute(kio, r)

    @Suppress("UNCHECKED_CAST")
    private fun <R, E, A> execute(kio: KIO<R, E, A>, r: R): Result<E, A> {

        val stack = RuntimeStack()

        var current: Any = kio
        while (true) {
            current = when (current) {
                is Eager<*, *, *> -> current.value
                is Lazy<*, *, *> -> current.valueF()
                is AskR<*, *, *> -> (current.accessF as (R) -> KIO<R, *, *>)(r)
                is FlatMap<*, *, *, *, *> -> {
                    stack.push(current.flatMapF as RuntimeFn)
                    current.prev
                }
                is Result<*, *> -> {
                    val fn = stack.pop()
                    if (fn != null)
                        fn(current)
                    else
                        return current as Result<E, A>
                }
                is LazySuspended<*, *, *> -> throw SuspensionNotSupported
                else -> throw NeverHereException
            }
        }
    }

    object NeverHereException : RuntimeException()
    object SuspensionNotSupported : RuntimeException("Please use the suspended runtime version in order to manage coroutines.")

}

