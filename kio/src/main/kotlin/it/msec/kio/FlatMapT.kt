package it.msec.kio

import it.msec.kio.common.tuple.T
import it.msec.kio.common.tuple.T2
import it.msec.kio.common.tuple.T3
import it.msec.kio.internals.KIOInternals.doFlatMap
import it.msec.kio.internals.KIOInternals.doResultMap
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.result.Failure
import it.msec.kio.result.Success

@JvmName("mapT2")
inline infix fun <R, E, A, B> KIO<R, E, A>.mapT(crossinline f: (A) -> B): KIO<R, E, T2<A, B>> = doResultMap {
    when (it) {
        is Success -> Success(T(it.value, f(it.value)))
        is Failure -> it
    }
}

@JvmName("flatMapT2")
inline infix fun <R, E, A, B> KIO<R, E, A>.flatMapT(crossinline f: (A) -> KIO<R, E, B>): KIO<R, E, T2<A, B>> = doFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T(it.value, v) }
        is Failure -> eager(it)
    }
}

@JvmName("mapT3")
inline infix fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.mapT(crossinline f: (T2<A, B>) -> C): KIO<R, E, T3<A, B, C>> = doResultMap {
    when (it) {
        is Success -> Success(T(it.value._1, it.value._2, f(it.value)))
        is Failure -> it
    }
}

@JvmName("flatMapT3")
inline infix fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.flatMapT(crossinline f: (T2<A, B>) -> KIO<R, E, C>): KIO<R, E, T3<A, B, C>> = doFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T(it.value._1, it.value._2, v) }
        is Failure -> eager(it)
    }
}
