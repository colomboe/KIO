package it.msec.kio

import it.msec.kio.NgFn.eager
import it.msec.kio.NgFn.evalAccessR
import it.msec.kio.NgFn.evalFlatMap
import it.msec.kio.NgFn.evalMap
import it.msec.kio.NgFn.execute
import it.msec.kio.NgFn.lazy
import it.msec.kio.core.T2
import it.msec.kio.core.T3

typealias BIO<E, A> = KIO<Any, E, A>
typealias EnvTask<R, A> = KIO<R, Nothing, A>
typealias Task<A> = EnvTask<Any, A>

fun <A> task(f: suspend () -> A): Task<A> = lazy { Ok(f()) }

fun <R, A> taskEnv(f: suspend () -> A): EnvTask<R, A> = lazy { Ok(f()) }

fun <A> just(v: A): Task<A> = eager(Ok(v))

fun <R, A> justEnv(v: A): EnvTask<R, A> = eager(Ok(v))

fun <E> failure(e: E): BIO<E, Nothing> = eager(Ko(e))

fun <R, E> failureEnv(e: E): KIO<R, E, Nothing> = eager(Ko(e))

inline fun <A> unsafe(crossinline f: suspend () -> A): KIO<Any, Throwable, A> = unsafeEnv(f)

inline fun <R, A> unsafeEnv(crossinline f: suspend () -> A): KIO<R, Throwable, A> = lazy {
    try {
        Ok(f())
    } catch (t: Throwable) {
        Ko(t)
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.map(crossinline f: (A) -> B): KIO<R, E, B> = evalMap {
    when (it) {
        is Ok -> Ok(f(it.value))
        is Ko -> it
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.flatMap(crossinline f: suspend (A) -> KIO<R, E, B>): KIO<R, E, B> = evalFlatMap {
    when (it) {
        is Ok -> f(it.value)
        is Ko -> eager(it)
    }
}

inline fun <R, A> askEnv(crossinline f: suspend (R) -> A): KIO<R, Nothing, A> =
        evalAccessR { justEnv(f(it)) }

inline fun <R, E, L, A> KIO<R, E, A>.mapError(crossinline f: (E) -> L): KIO<R, L, A> = evalMap {
    when (it) {
        is Ok -> it
        is Ko -> Ko(f(it.error))
    }
}

fun <R, E, A> KIO<R, E, A>.swap(): KIO<R, A, E> = evalMap {
    when (it) {
        is Ok -> Ko(it.value)
        is Ko -> Ok(it.error)
    }
}

fun <R, A> EnvTask<R, A>.attempt(): KIO<R, Throwable, A> =
        evalAccessR { env -> unsafeEnv { (this@attempt.execute(env) as Ok<A>).value } }


inline fun <R, E, A> KIO<R, E, A>.recover(crossinline f: (E) -> A): EnvTask<R, A> = evalFlatMap {
    when (it) {
        is Ok -> eager(it)
        is Ko -> taskEnv<R, A> { f(it.error) }
    }
}

inline fun <R, E, A> KIO<R, E, A>.tryRecover(crossinline f: suspend (E) -> KIO<R, E, A>): KIO<R, E, A> = evalFlatMap {
    when (it) {
        is Ok -> eager(it)
        is Ko -> f(it.error)
    }
}

fun <R, E, A, C> KIO<R, E, A>.fold(e: (E) -> C, f: (A) -> C): EnvTask<R, C> =
    map(f).recover(e)

inline fun <R, E, A, B> KIO<R, E, A>.flatMapT2(crossinline f: (A) -> KIO<R, E, B>): KIO<R, E, T2<A, B>> = evalFlatMap {
    when (it) {
        is Ok -> f(it.value).map { v -> T2(it.value, v) }
        is Ko -> eager(it)
    }
}

inline fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.flatMapT3(crossinline f: (T2<A, B>) -> KIO<R, E, C>): KIO<R, E, T3<A, B, C>> = evalFlatMap {
    when (it) {
        is Ok -> f(it.value).map { v -> T3(it.value._1, it.value._2, v) }
        is Ko -> eager(it)
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.mapT2(crossinline f: (A) -> B): KIO<R, E, T2<A, B>> = evalMap {
    when (it) {
        is Ok -> Ok(T2(it.value, f(it.value)))
        is Ko -> it
    }
}

inline fun <R, E, A, B, C> KIO<R, E, T2<A, B>>.mapT3(crossinline f: (T2<A, B>) -> C): KIO<R, E, T3<A, B, C>> = evalMap {
    when (it) {
        is Ok -> Ok(T3(it.value._1, it.value._2, f(it.value)))
        is Ko -> it
    }
}

inline fun <R, E, A, L, B> KIO<R, E, A>.bimap(crossinline f: (E) -> L, crossinline g: (A) ->B): KIO<R, L, B> = evalMap {
    when (it) {
        is Ok -> Ok(g(it.value))
        is Ko -> Ko(f(it.error))
    }
}
