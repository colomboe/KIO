package it.msec.kio.concurrent

import it.msec.kio.*

fun <R, E, A> KIO<R, E, A>.fork(): URIO<R, DeferredResult<E, A>> =
    ask { r -> Fork(this@fork, r) }

fun <E, A> DeferredResult<E, A>.await(): IO<E, A> = Await(this)
