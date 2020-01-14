package it.msec.kio

import it.msec.kio.common.tuple.T
import it.msec.kio.common.tuple.T2
import it.msec.kio.runtime.Runtime
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <A> UIO<A>.unsafeRunSyncAndGet(ctx: CoroutineContext = EmptyCoroutineContext) =
        Runtime.unsafeRunSyncAndGet(this)

fun <E, A> IO<E, A>.unsafeRunSync(ctx: CoroutineContext = EmptyCoroutineContext) =
        Runtime.unsafeRunSync(this)

fun <R, E, A> KIO<R, E, A>.unsafeRunSync(env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
        Runtime.unsafeRunSync(this, env)

inline fun <A> runAndGetTimeMillis(crossinline f: () -> A): T2<A, Long> {
    val start = System.currentTimeMillis()
    val result = f()
    val end = System.currentTimeMillis()
    return T(result, end - start)
}

suspend inline fun <A> CoroutineScope.runSuspendedAndGetTimeMillis(crossinline f: suspend CoroutineScope.() -> A): T2<A, Long> {
    val start = System.currentTimeMillis()
    val result = f()
    val end = System.currentTimeMillis()
    return T(result, end - start)
}
