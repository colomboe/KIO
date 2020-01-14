package it.msec.kio.runtime

import it.msec.kio.IO
import it.msec.kio.KIO
import it.msec.kio.UIO
import it.msec.kio.result.Result
import it.msec.kio.result.get
import it.msec.kio.runtime.CoroutineInterpreter.execute
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

}

