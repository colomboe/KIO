package it.msec.kio

import it.msec.kio.internals.KIOInternals.doFlatMap
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.result.Cancelled
import it.msec.kio.result.Failure
import it.msec.kio.result.Success

inline fun <R, E, A, L, B> KIO<R, E, A>.peek(crossinline f: (A) -> KIO<R, L, B>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> f(it.value).doFlatMap { r ->
            when (r) {
                is Cancelled -> eager<R, Nothing, Nothing>(r)
                else -> eager<R, E, A>(it)
            }
        }
        is Failure -> eager<R, E, A>(it)
        is Cancelled -> eager<R, E, A>(it)
    }
}

inline fun <R, E, A, L, B> KIO<R, E, A>.peekError(crossinline f: (E) -> KIO<R, L, B>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> eager<R, E, A>(it)
        is Failure -> f(it.error).doFlatMap { result ->
            when (result) {
                is Cancelled -> eager<R, Nothing, Nothing>(result)
                else -> eager<R, E, A>(it)
            }
        }
        is Cancelled -> eager<R, E, A>(it)
    }
}
