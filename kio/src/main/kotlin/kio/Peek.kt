package kio

import kio.internals.KIOInternals.doFlatMap
import kio.internals.KIOInternals.eager
import kio.result.Failure
import kio.result.Success

inline fun <R, E, A, L, B> KIO<R, E, A>.peek(crossinline f: (A) -> KIO<R, L, B>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> f(it.value).doFlatMap { _ -> eager<R, E, A>(it) }
        is Failure -> eager<R, E, A>(it)
    }
}

inline fun <R, E, A, L, B> KIO<R, E, A>.peekError(crossinline f: (E) -> KIO<R, L, B>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> eager<R, E, A>(it)
        is Failure -> f(it.error).doFlatMap { _ -> eager<R, E, A>(it) }
    }
}
