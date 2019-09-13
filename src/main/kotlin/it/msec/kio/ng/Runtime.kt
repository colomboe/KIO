package it.msec.kio.ng

import it.msec.kio.BIO
import it.msec.kio.KIO
import it.msec.kio.NgFn.execute
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <E, A> BIO<E, A>.unsafeRunSync(ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(Unit) }

fun <R, E, A> KIO<R, E, A>.unsafeRunSync(env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(env) }

suspend fun <R, E, A> KIO<R, E, A>.unsafeRunSuspended(env: R) = this.execute(env)
