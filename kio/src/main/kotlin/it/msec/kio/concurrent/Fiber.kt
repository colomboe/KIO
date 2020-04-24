package it.msec.kio.concurrent

import it.msec.kio.*

fun <R, E, A> KIO<R, E, A>.fork(): URIO<R, DeferredResult<E, A>> =
    ask { r -> Fork(this, r) }

fun <E, A> DeferredResult<E, A>.await(): IO<E, A> = Await(this)
fun <R, E, A> DeferredResult<E, A>.awaitR(): KIO<R, E, A> = Await(this)
