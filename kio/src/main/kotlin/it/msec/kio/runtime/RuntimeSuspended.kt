package it.msec.kio.runtime

import it.msec.kio.*
import it.msec.kio.result.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
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

        var ignoreCancellation = false
        var r: Any? = initialR
        var current: Any = k
        while (true) {
            current = when (current) {

                is Eager<*, *, *> -> current.value

                is Lazy<*, *, *> -> try {
                    val newResult = current.valueF()
                    if (ignoreCancellation || isActive) newResult else Cancelled(CancellationException())
                } catch (c: CancellationException) {
                    Cancelled(c)
                } catch (t: Throwable) {
                    Failure(t)
                }

                is LazySuspended<*, *, *> -> try {
                    val suspendedF = current.suspendedF
                    val newResult: Any = if (ignoreCancellation)
                        withContext(NonCancellable) { suspendedF.invoke(this) }
                    else
                        suspendedF(this)
                    if (ignoreCancellation || isActive) newResult else Cancelled(CancellationException())
                } catch (c: CancellationException) {
                    Cancelled(c)
                } catch (t: Throwable) {
                    Failure(t)
                }

                is AskR<*, *, *> -> (current.accessF as (R) -> KIO<R, *, *>)(r as R)

                is SuccessMap<*, *, *, *> -> {
                    stack.push(successMapToF(current))
                    current.prev
                }

                is FlatMap<*, *, *, *, *> -> {
                    stack.push(current.flatMapF as RuntimeFn)
                    current.prev
                }

                is ForceState<*, *, *> -> {
                    ignoreCancellation = current.ignoreCancellation
                    current.result
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
                    Success(Fiber(deferred))
                }

                is Await<*, *, *> -> try {
                    current.fiber.deferred.await()
                }
                catch(e: CancellationException) {
                    Cancelled(e)
                }

                is Cancel<*, *, *> -> Success(current.fiber.deferred.cancel())

                is Race<*, *, *, *, *, *, *> -> {
                    val d1 = current.fiber1 as Fiber<Any?, Any?>
                    val d2 = current.fiber2 as Fiber<Any?, Any?>
                    val f1 = current.f1  as (Result<Any?, Any?>, Fiber<Any?, Any?>) -> KIO<Any?, Any?, Any?>
                    val f2 = current.f2  as (Result<Any?, Any?>, Fiber<Any?, Any?>) -> KIO<Any?, Any?, Any?>
                    select {
                        d1.deferred.onAwait {
                            @OptIn(ExperimentalCoroutinesApi::class)
                            f1(d1.deferred.getCompleted(), d2)
                        }
                        d2.deferred.onAwait {
                            @OptIn(ExperimentalCoroutinesApi::class)
                            f2(d2.deferred.getCompleted(), d1)
                        }
                    }
                }

                else -> throw NeverHereException
            }
        }
    }

    object NeverHereException : RuntimeException()

}

