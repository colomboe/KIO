package it.msec.sparrow

import it.msec.sparrow.EvalFn.evalFlatMap
import it.msec.sparrow.EvalFn.evalMap
import it.msec.sparrow.EvalFn.execute
import it.msec.sparrow.EvalFn.later
import it.msec.sparrow.EvalFn.now

sealed class Result<out E, out A>
data class Success<A>(val value: A) : Result<Nothing, A>()
data class Failure<E>(val error: E) : Result<E, Nothing>()

typealias BIO<E, A> = Eval<Result<E, A>>

fun <E, A> fromResult(r: Result<E, A>): BIO<E, A> = later { r }

fun <A> just(v: A): Task<A> = now(Success(v))

fun <E> failure(e: E): BIO<E, Nothing> = now(Failure(e))

inline fun <A> unsafe(crossinline f: () -> A): BIO<Throwable, A> = later {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <E, A, B> BIO<E, A>.map(crossinline f: (A) -> B): BIO<E, B> = evalMap {
    when (it) {
        is Success -> Success(f(it.value))
        is Failure -> it
    }
}

inline fun <E, A, B> BIO<E, A>.flatMap(crossinline f: (A) -> BIO<E, B>): BIO<E, B> = evalFlatMap {
    when (it) {
        is Success -> f(it.value)
        is Failure -> now(it)
    }
}

inline fun <E, L, A> BIO<E, A>.mapError(crossinline f: (E) -> L): BIO<L, A> = evalMap {
    when (it) {
        is Success -> it
        is Failure -> Failure(f(it.error))
    }
}

fun <E, A> BIO<E, A>.swap(): BIO<A, E> = evalMap {
    when (it) {
        is Success -> Failure(it.value)
        is Failure -> Success(it.error)
    }
}

fun <A> Task<A>.attempt(): BIO<Throwable, A> = unsafe { this.execute().value }

inline fun <E, A> BIO<E, A>.recover(crossinline f: (E) -> A): Task<A> = evalFlatMap {
    when (it) {
        is Success -> now(it)
        is Failure -> task { f(it.error) }
    }
}

inline fun <E, A> BIO<E, A>.tryRecover(crossinline f: (E) -> BIO<E, A>): BIO<E, A> = evalFlatMap {
    when (it) {
        is Success -> now(it)
        is Failure -> f(it.error)
    }
}

fun <E, A> BIO<E, A>.unsafeRunSync() = this.execute()

fun <E, A, C> BIO<E, A>.fold(e: (E) -> C, f: (A) -> C): Task<C> =
    map(f).recover(e)

inline fun <E, A, B> BIO<E, A>.flatMapT2(crossinline f: (A) -> BIO<E, B>): BIO<E, T2<A,B>> = evalFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T2(it.value, v) }
        is Failure -> now(it)
    }
}

inline fun <E, A, B, C> BIO<E, T2<A, B>>.flatMapT3(crossinline f: (T2<A, B>) -> BIO<E, C>): BIO<E, T3<A,B, C>> = evalFlatMap {
    when (it) {
        is Success -> f(it.value).map { v -> T3(it.value._1, it.value._2, v) }
        is Failure -> now(it)
    }
}

inline fun <E, A, B> BIO<E, A>.mapT2(crossinline f: (A) -> B): BIO<E, T2<A,B>> = evalMap {
    when (it) {
        is Success -> Success(T2(it.value, f(it.value)))
        is Failure -> it
    }
}

inline fun <E, A, B, C> BIO<E, T2<A, B>>.mapT3(crossinline f: (T2<A, B>) -> C): BIO<E, T3<A,B, C>> = evalMap {
    when (it) {
        is Success -> Success(T3(it.value._1, it.value._2, f(it.value)))
        is Failure -> it
    }
}

