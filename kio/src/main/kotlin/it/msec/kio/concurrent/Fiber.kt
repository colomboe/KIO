package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSuspended
import kotlinx.coroutines.async

fun <R, E, A> KIO<R, E, A>.fork(): URIO<R, DeferredResult<E, A>> =
    ask { r -> Fork { async { unsafeRunSuspended(this@fork, r) } } }

fun <E, A> DeferredResult<E, A>.await(): IO<E, A> = Await(this)
