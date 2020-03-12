package it.msec.kio

import it.msec.kio.internals.KIOInternals.doFlatMap
import it.msec.kio.internals.KIOInternals.eager

fun <R, E, A, B> KIO<R, E, A>.ensuring(finally: URIO<R, B>): KIO<R, E, A> = doFlatMap { result ->
    finally.doFlatMap { eager<R, E, A>(result) }
}

fun <R, E, A, B, C> KIO<R, E, A>.bracket(finally: (A) -> URIO<R, B>, process: (A) -> KIO<R, E, C>): KIO<R, E, C> =
        this.flatMap { a -> process(a).ensuring(finally(a)) }
