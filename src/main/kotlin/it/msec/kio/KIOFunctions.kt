package it.msec.kio

import it.msec.kio.common.tuple.T
import it.msec.kio.common.tuple.T2
import it.msec.kio.common.tuple.T3
import it.msec.kio.internals.KIOInternals.doAccessR
import it.msec.kio.internals.KIOInternals.doFlatMap
import it.msec.kio.internals.KIOInternals.doMap
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.internals.KIOInternals.execute
import it.msec.kio.internals.KIOInternals.lazy
import it.msec.kio.result.Failure
import it.msec.kio.result.Success

fun <A> task(f: suspend () -> A): Task<A> = lazy { Success(f()) }

fun <R, A> taskR(f: suspend () -> A): TaskR<R, A> = lazy { Success(f()) }

fun <A> just(v: A): Task<A> = eager(Success(v))

fun <R, A> justR(v: A): TaskR<R, A> = eager(Success(v))

fun <E> failure(e: E): BIO<E, Nothing> = eager(Failure(e))

fun <R, E> failureR(e: E): KIO<R, E, Nothing> = eager(Failure(e))

inline fun <A> unsafe(crossinline f: suspend () -> A): KIO<Any, Throwable, A> = lazy {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, A> unsafeR(crossinline f: suspend () -> A): KIO<R, Throwable, A> = lazy {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.map(crossinline f: (A) -> B): KIO<R, E, B> = doMap {
    when (it) {
        is Success -> Success(f(it.value))
        is Failure -> it
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.flatMap(crossinline f: (A) -> KIO<R, E, B>): KIO<R, E, B> = doFlatMap {
    when (it) {
        is Success -> f(it.value)
        is Failure -> eager(it)
    }
}

inline fun <R, A> askR(crossinline f: (R) -> A): KIO<R, Nothing, A> =
        doAccessR { justR(f(it)) }

inline fun <R, E, L, A> KIO<R, E, A>.mapError(crossinline f: (E) -> L): KIO<R, L, A> = doMap {
    when (it) {
        is Success -> it
        is Failure -> Failure(f(it.error))
    }
}

fun <R, E, A> KIO<R, E, A>.swap(): KIO<R, A, E> = doMap {
    when (it) {
        is Success -> Failure(it.value)
        is Failure -> Success(it.error)
    }
}

fun <R, A> TaskR<R, A>.attempt(): KIO<R, Throwable, A> =
        doAccessR { env -> unsafeR { (this@attempt.execute(env) as Success<A>).value } }


inline fun <R, E, A> KIO<R, E, A>.recover(crossinline f: (E) -> A): TaskR<R, A> = doFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> taskR<R, A> { f(it.error) }
    }
}

inline fun <R, E, A> KIO<R, E, A>.tryRecover(crossinline f: (E) -> KIO<R, E, A>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> f(it.error)
    }
}

fun <R, E, A, C> KIO<R, E, A>.fold(e: (E) -> C, f: (A) -> C): TaskR<R, C> =
    map(f).recover(e)

inline fun <R, E, A, L, B> KIO<R, E, A>.bimap(crossinline f: (E) -> L, crossinline g: (A) ->B): KIO<R, L, B> = doMap {
    when (it) {
        is Success -> Success(g(it.value))
        is Failure -> Failure(f(it.error))
    }
}

inline fun <R, E, A> KIO<R, E, A>.filterTo(crossinline e: (A) -> E, crossinline f: (A) -> Boolean): KIO<R, E, A> = doMap {
    when (it) {
        is Success -> if (f(it.value)) it else Failure(e(it.value))
        is Failure -> it
    }
}

@JvmName("mapT2")
inline fun <R, E, A, B> KIO<R, E, A>.mapT(crossinline f: (A) -> B): KIO<R, E, T2<A, B>> = doMap {
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
inline fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.mapT(crossinline f: (T2<A, B>) -> C): KIO<R, E, T3<A, B, C>> = doMap {
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
