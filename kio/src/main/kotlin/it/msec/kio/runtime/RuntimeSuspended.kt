package it.msec.kio.runtime

import it.msec.kio.*
import it.msec.kio.common.coroutines.joinFirst
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success
import it.msec.kio.result.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object RuntimeSuspended : KIORuntime {

    override fun <A> unsafeRunSyncAndGet(kio: UIO<A>): A = runBlocking { execute(kio, Unit) }.get()

    override fun <E, A> unsafeRunSync(kio: IO<E, A>): Result<E, A> = runBlocking { execute(kio, Unit) }

    override fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R): Result<E, A> = runBlocking { execute(kio, r) }

    fun <A> unsafeRunSyncAndGet(kio: UIO<A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, Unit) }.get()

    fun <E, A> unsafeRunSync(kio: IO<E, A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, Unit) }

    fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
            runBlocking(ctx) { execute(kio, env) }

    suspend fun <R, E, A> CoroutineScope.unsafeRunSuspended(kio: KIO<R, E, A>, env: R) =
            execute(kio, env)

    @Suppress("UNCHECKED_CAST")
    private suspend fun <R, E, A> CoroutineScope.execute(k: KIO<R, E, A>, initialR: R): Result<E, A> {

        val stack = RuntimeStack()

        var r: Any? = initialR
        var current: Any = k
        while (true) {
            current = when (current) {
                is Eager<*, *, *> -> current.value
                is Lazy<*, *, *> -> try { current.valueF() } catch(t: Throwable) { Failure(t) }
                is LazySuspended<*, *, *> -> try { current.suspendedF(this) } catch(t: Throwable) { Failure(t) }
                is AskR<*, *, *> -> (current.accessF as (R) -> KIO<R, *, *>)(r as R)
                is SuccessMap<*, *, *, *> -> {
                    stack.push(successMapToF(current))
                    current.prev
                }
                is FlatMap<*, *, *, *, *> -> {
                    stack.push(current.flatMapF as RuntimeFn)
                    current.prev
                }
                is Attempt<*, *> -> current.urio
                is Result<*, *> -> {
                    val fn = stack.pop()
                    if (fn != null) fn(current) else return current as Result<E, A>
                }
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
                is Fork<*, *, *> -> {
                    val program = current.program as KIO<Any?, Any?, Any?>
                    val env = current.env
                    val deferred = this.async { execute(program, env) }
                    Success(DeferredResult(deferred))
                }
                is Await<*, *, *> -> current.fiber.deferred.await()
                is Race<*, *, *, *, *, *, *> -> {
                    val winner = joinFirst(current.d1.deferred, current.d2.deferred)
                    when (winner) {
                        current.d1.deferred -> (current.f1 as (Result<Any?, Any?>) -> KIO<Any?, Any?, Any?>)(winner.getCompleted())
                        else -> (current.f2 as (Result<Any?, Any?>) -> KIO<Any?, Any?, Any?>)(winner.getCompleted())
                    }
                }
                else -> throw NeverHereException
            }
        }
    }

    object NeverHereException : RuntimeException()

}

