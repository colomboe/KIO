package kio

import kio.common.tuple.T
import kio.common.tuple.T2
import kio.common.tuple.T3
import kio.internals.KIOInternals.doFlatMap
import kio.internals.KIOInternals.doResultMap
import kio.internals.KIOInternals.eager
import kio.result.Failure
import kio.result.Success

@JvmName("mapT2")
inline fun <R, E, A, B> KIO<R, E, A>.mapT(crossinline f: (A) -> B): KIO<R, E, T2<A, B>> = doResultMap {
    when (it) {
        is Success -> Success(T(it.value, f(it.value)))
        is Failure -> it
    }
}

@JvmName("flatMapT2")
inline fun <R, E, A, B> KIO<R, E, A>.flatMapT(crossinline f: (A) -> KIO<R, E, B>): KIO<R, E, T2<A, B>> = doFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T(it.value, v) }
        is Failure -> eager(it)
    }
}

@JvmName("mapT3")
inline fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.mapT(crossinline f: (T2<A, B>) -> C): KIO<R, E, T3<A, B, C>> = doResultMap {
    when (it) {
        is Success -> Success(T(it.value._1, it.value._2, f(it.value)))
        is Failure -> it
    }
}

@JvmName("flatMapT3")
inline fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.flatMapT(crossinline f: (T2<A, B>) -> KIO<R, E, C>): KIO<R, E, T3<A, B, C>> = doFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T(it.value._1, it.value._2, v) }
        is Failure -> eager(it)
    }
}
