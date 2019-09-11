package it.msec.sparrow

import it.msec.sparrow.EvalFn.execute
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <E, A> BIO<E, A>.unsafeRunSync(ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(null) }

fun <R, E, A> EnvIO<R, E, A>.unsafeRunSync(env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
        runBlocking(ctx) { this@unsafeRunSync.execute(env) }

suspend fun <R, E, A> EnvIO<R, E, A>.unsafeRunSuspended(env: R) = this.execute(env)
