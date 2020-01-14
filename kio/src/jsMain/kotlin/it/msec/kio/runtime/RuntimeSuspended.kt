package it.msec.kio.runtime

import it.msec.kio.IO
import it.msec.kio.KIO
import it.msec.kio.UIO
import it.msec.kio.result.Result
import it.msec.kio.result.get
import it.msec.kio.runtime.CoroutineInterpreter.execute
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.js.Promise

object RuntimeSuspended {

    fun <A> unsafeRunPromiseAndGet(kio: UIO<A>): Promise<A> = GlobalScope.promise { execute(kio, Unit).get() }

    fun <E, A> unsafeRunPromise(kio: IO<E, A>): Promise<Result<E, A>> = GlobalScope.promise { execute(kio, Unit) }

    fun <R, E, A> unsafeRunPromise(kio: KIO<R, E, A>, r: R): Promise<Result<E, A>> = GlobalScope.promise { execute(kio, r) }

    fun <A> unsafeRunPromiseAndGet(kio: UIO<A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            GlobalScope.promise(ctx) { execute(kio, Unit).get() }

    fun <E, A> unsafeRunPromise(kio: IO<E, A>, ctx: CoroutineContext = EmptyCoroutineContext) =
            GlobalScope.promise(ctx) { execute(kio, Unit) }

    fun <R, E, A> unsafeRunPromise(kio: KIO<R, E, A>, env: R, ctx: CoroutineContext = EmptyCoroutineContext) =
            GlobalScope.promise(ctx) { execute(kio, env) }

}

