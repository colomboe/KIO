package it.msec.kio.runtime

import it.msec.kio.*
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.get

object Runtime : KIORuntime {

    override fun <A> unsafeRunSyncAndGet(kio: UIO<A>) =
            execute(kio, Unit).get()

    override fun <E, A> unsafeRunSync(kio: IO<E, A>) =
            execute(kio, Unit)

    override fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R) =
            execute(kio, r)

    @Suppress("UNCHECKED_CAST")
    private fun <R, E, A> execute(k: KIO<R, E, A>, initialR: R): Result<E, A> {

        val stack = RuntimeStack()

        var r: Any? = initialR
        var current: Any = k
        while (true) {
            current = when (current) {
                is Eager<*, *, *> -> current.value
                is Lazy<*, *, *> -> try { current.valueF() } catch(t: Throwable) { Failure(t) }
                is AskR<*, *, *> -> (current.accessF as (R) -> KIO<R, *, *>)(r as R)
                is SuccessMap<*, *, *, *> -> {
                    stack.push(successMapToF(current))
                    current.prev
                }
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
                is Attempt<*, *> -> current.urio
                is ProvideR<*, *, *> -> {
                    val prevR = r
                    stack.push { result -> RestoreR(prevR, result) }
                    r = current.r
                    current.prev
                }
                is RestoreR<*, *, *> -> {
                    r = current.r
                    current.value
                }
                is LazySuspended<*, *, *> -> throw SuspensionNotSupported
                is Fork<*, *, *> -> throw SuspensionNotSupported
                is Await<*, *, *> -> throw SuspensionNotSupported
                else -> throw NeverHereException
            }
        }
    }

    object NeverHereException : RuntimeException()
    object SuspensionNotSupported : RuntimeException("Please use the suspended runtime version in order to manage coroutines.")

}

