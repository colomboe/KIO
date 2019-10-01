package it.msec.kio.runtime.v2

import it.msec.kio.*
import it.msec.kio.result.Result
import it.msec.kio.result.get
import it.msec.kio.runtime.RuntimeStack
import it.msec.kio.runtime.RuntimeSuspendFn
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object RuntimeV2 {

    fun <A> unsafeRunSyncAndGet(kio: UIO<A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, Unit) }.get()

    fun <E, A> unsafeRunSync(kio: IO<E, A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, Unit) }

    fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, env) }

    suspend fun <R, E, A> KIO<R, E, A>.unsafeRunSuspended(env: R) =
            execute(this, env)


    @Suppress("UNCHECKED_CAST")
    private suspend fun <R, E, A> execute(kio: KIO<R, E, A>, r: R): Result<E, A> {

        val stack = RuntimeStack()

        var current: Any = kio
        while (true) {
            current = when (current) {
                is Eager<*, *, *> -> current.value
                is Lazy<*, *, *> -> current.valueF()
                is EnvAccess<*, *, *> -> (current.accessF as suspend (R) -> KIO<R, *, *>)(r)
                is FlatMap<*, *, *, *, *> -> {
                    stack.push(current.flatMapF as RuntimeSuspendFn)
                    current.prev
                }
                is Result<*, *> -> {
                    val fn = stack.pop()
                    if (fn != null) fn(current) else return current as Result<E, A>
                }
                else -> throw NeverHereException
            }
        }
    }

    object NeverHereException : RuntimeException()

}

