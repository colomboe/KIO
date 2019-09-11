package it.msec.sparrow

import it.msec.sparrow.EvalFn.eager
import it.msec.sparrow.EvalFn.evalAccessEnv
import it.msec.sparrow.EvalFn.evalFlatMap
import it.msec.sparrow.EvalFn.evalMap
import it.msec.sparrow.EvalFn.execute
import it.msec.sparrow.EvalFn.lazy

typealias EnvIO<R, E, A> = Eval<R, Result<E, A>>
typealias BIO<E, A> = EnvIO<Nothing, E, A>
typealias EnvTask<R, A> = Eval<R, Success<A>>
typealias Task<A> = EnvTask<Nothing, A>

fun <A> task(f: suspend () -> A): Task<A> = lazy { Success(f()) }

fun <A> just(v: A): Task<A> = eager(Success(v))

fun <E> failure(e: E): BIO<E, Nothing> = eager(Failure(e))

inline fun <A> unsafe(crossinline f: suspend () -> A): BIO<Throwable, A> = lazy {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, E, A, B> EnvIO<R, E, A>.map(crossinline f: (A) -> B): EnvIO<R, E, B> = evalMap {
    when (it) {
        is Success -> Success(f(it.value))
        is Failure -> it
    }
}

inline fun <R, E, A, B> EnvIO<R, E, A>.flatMap(crossinline f: suspend (A) -> EnvIO<R, E, B>): EnvIO<R, E, B> = evalFlatMap {
    when (it) {
        is Success -> f(it.value)
        is Failure -> eager(it)
    }
}

inline fun <R, E, A, B> EnvIO<R, E, A>.accessEnvIO(crossinline f: suspend (R) -> EnvIO<R, E, B>): EnvIO<R, E, B> =
        evalAccessEnv { f(it) }

fun <R, E, A, B> EnvIO<R, E, A>.accessEnv(f: suspend (R) -> B): EnvIO<R, E, B> =
        evalAccessEnv { just(f(it)) }

inline fun <R, E, L, A> EnvIO<R, E, A>.mapError(crossinline f: (E) -> L): EnvIO<R, L, A> = evalMap {
    when (it) {
        is Success -> it
        is Failure -> Failure(f(it.error))
    }
}

fun <R, E, A> EnvIO<R, E, A>.swap(): EnvIO<R, A, E> = evalMap {
    when (it) {
        is Success -> Failure(it.value)
        is Failure -> Success(it.error)
    }
}

fun <R, A> EnvTask<R, A>.attempt(): EnvIO<R, Throwable, A> =
        evalAccessEnv { env -> unsafe { this@attempt.execute(env).value } }


inline fun <R, E, A> EnvIO<R, E, A>.recover(crossinline f: (E) -> A): EnvTask<R, A> = evalFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> task { f(it.error) }
    }
}

inline fun <R, E, A> EnvIO<R, E, A>.tryRecover(crossinline f: suspend (E) -> EnvIO<R, E, A>): EnvIO<R, E, A> = evalFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> f(it.error)
    }
}

fun <R, E, A, C> EnvIO<R, E, A>.fold(e: (E) -> C, f: (A) -> C): EnvTask<R, C> =
    map(f).recover(e)

inline fun <R, E, A, B> EnvIO<R, E, A>.flatMapT2(crossinline f: (A) -> EnvIO<R, E, B>): EnvIO<R, E, T2<A,B>> = evalFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T2(it.value, v) }
        is Failure -> eager(it)
    }
}

inline fun <R, E, A, B, C> EnvIO<R, E, T2<A, B>>.flatMapT3(crossinline f: (T2<A, B>) -> EnvIO<R, E, C>): EnvIO<R, E, T3<A,B, C>> = evalFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T3(it.value._1, it.value._2, v) }
        is Failure -> eager(it)
    }
}

inline fun <R, E, A, B> EnvIO<R, E, A>.mapT2(crossinline f: (A) -> B): EnvIO<R, E, T2<A,B>> = evalMap {
    when (it) {
        is Success -> Success(T2(it.value, f(it.value)))
        is Failure -> it
    }
}

inline fun <R, E, A, B, C> EnvIO<R, E, T2<A, B>>.mapT3(crossinline f: (T2<A, B>) -> C): EnvIO<R, E, T3<A,B, C>> = evalMap {
    when (it) {
        is Success -> Success(T3(it.value._1, it.value._2, f(it.value)))
        is Failure -> it
    }
}

