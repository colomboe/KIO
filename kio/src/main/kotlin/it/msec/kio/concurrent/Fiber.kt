package it.msec.kio.concurrent

import it.msec.kio.*

fun <R, E, A> KIO<R, E, A>.fork(): URIO<R, Fiber<E, A>> =
    ask { r -> Fork(this, r) }

fun <E, A> Fiber<E, A>.await(): IO<E, A> = Await(this)
fun <R, E, A> Fiber<E, A>.awaitR(): KIO<R, E, A> = Await(this)
fun Fiber<*, *>.cancel(): IO<Nothing, Unit> = Cancel(this)
fun <R> Fiber<*, *>.cancelR(): KIO<R, Nothing, Unit> = Cancel(this)
