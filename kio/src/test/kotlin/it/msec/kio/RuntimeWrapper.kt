package it.msec.kio

import it.msec.kio.runtime.v2.RuntimeV2
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <A> UIO<A>.unsafeRunSyncAndGet(ctx: CoroutineContext = EmptyCoroutineContext) =
        RuntimeV2.unsafeRunSyncAndGet(this)

fun <E, A> IO<E, A>.unsafeRunSync(ctx: CoroutineContext = EmptyCoroutineContext) =
        RuntimeV2.unsafeRunSync(this)

fun <R, E, A> KIO<R, E, A>.unsafeRunSync(env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
        RuntimeV2.unsafeRunSync(this, env)

//suspend fun <R, E, A> KIO<R, E, A>.unsafeRunSuspended(env: R) =
//        RuntimeV2.unsafeRunSuspended(this, env)

