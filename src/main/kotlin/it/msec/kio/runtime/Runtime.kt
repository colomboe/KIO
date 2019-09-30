package it.msec.kio.runtime

import it.msec.kio.IO
import it.msec.kio.KIO
import it.msec.kio.UIO
import it.msec.kio.internals.KIOInternals.execute
import it.msec.kio.result.get
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <A> UIO<A>.unsafeRunSyncAndGet(ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSyncAndGet.execute(Unit) }.get()

fun <E, A> IO<E, A>.unsafeRunSync(ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(Unit) }

fun <R, E, A> KIO<R, E, A>.unsafeRunSync(env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(env) }

suspend fun <R, E, A> KIO<R, E, A>.unsafeRunSuspended(env: R) = this.execute(env)
