package it.msec.kio

import it.msec.kio.internals.KIOInternals.doAskR
import it.msec.kio.internals.KIOInternals.doFlatMap
import it.msec.kio.internals.KIOInternals.doResultMap
import it.msec.kio.internals.KIOInternals.doSuccessMap
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.internals.KIOInternals.lazy
import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Failure
import it.msec.kio.result.Success

fun <A> effect(f: () -> A): UIO<A> = lazy { Success(f()) }

fun <R, A> effectR(f: () -> A): URIO<R, A> = lazy { Success(f()) }

fun <A> just(v: A): UIO<A> = eager(Success(v))

fun <R, A> justR(v: A): URIO<R, A> = eager(Success(v))

fun <E> failure(e: E): IO<E, Nothing> = eager(Failure(e))

fun <R, E> failureR(e: E): KIO<R, E, Nothing> = eager(Failure(e))

inline fun <A> unsafe(crossinline f: () -> A): KIO<Any, Throwable, A> = lazy {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, A> unsafeR(crossinline f: () -> A): KIO<R, Throwable, A> = lazy {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <A> suspended(crossinline f: suspend () -> A): UIO<A> = lazySuspended { Success(f()) }

inline fun <R, A> suspendedR(crossinline f: suspend () -> A): URIO<R, A> = lazySuspended { Success(f()) }

inline fun <A> unsafeSuspended(crossinline f: suspend () -> A): KIO<Any, Throwable, A> = lazySuspended {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, A> unsafeSuspendedR(crossinline f: suspend () -> A): KIO<R, Throwable, A> = lazySuspended {
    try {
        Success(f())
    } catch (t: Throwable) {
        Failure(t)
    }
}

inline fun <R, E, A> ask(crossinline f: (R) -> KIO<R, E, A>): KIO<R, E, A> = doAskR { f(it) }

inline fun <R, A> askPure(crossinline f: (R) -> A): URIO<R, A> = doAskR { justR(f(it)) }

fun <R> askPure(): URIO<R, R> = doAskR(::justR)

inline fun <R, E, A> withR(crossinline f: R.() -> KIO<R, E, A>): KIO<R, E, A> = doAskR { f(it) }

inline fun <R, A> withPureR(crossinline f: R.() -> A): URIO<R, A> = doAskR { justR(f(it)) }

inline infix fun <R, E, A, B> KIO<R, E, A>.map(noinline f: (A) -> B): KIO<R, E, B> = doSuccessMap(f)

inline infix fun <R, E, A, B> KIO<R, E, A>.flatMap(crossinline f: (A) -> KIO<R, E, B>): KIO<R, E, B> = doFlatMap {
    when (it) {
        is Success -> f(it.value)
        is Failure -> eager(it)
    }
}

inline fun <R, E, L, A> KIO<R, E, A>.mapError(crossinline f: (E) -> L): KIO<R, L, A> = doResultMap {
    when (it) {
        is Success -> it
        is Failure -> Failure(f(it.error))
    }
}

inline fun <R, E, A, B> KIO<R, E, A>.followedBy(k: KIO<R, E, B>): KIO<R, E, B> = doFlatMap {
    when (it) {
        is Success -> k
        is Failure -> eager(it)
    }
}

fun <R, E, A> KIO<R, E, A>.swap(): KIO<R, A, E> = doResultMap {
    when (it) {
        is Success -> Failure(it.value)
        is Failure -> Success(it.error)
    }
}

inline fun <R, E, A> KIO<R, E, A>.recover(crossinline f: (E) -> A): URIO<R, A> = doFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> effectR<R, A> { f(it.error) }
    }
}

inline fun <R, E, A> KIO<R, E, A>.tryRecover(crossinline f: (E) -> KIO<R, E, A>): KIO<R, E, A> = doFlatMap {
    when (it) {
        is Success -> eager(it)
        is Failure -> f(it.error)
    }
}

inline fun <R, E, A, C> KIO<R, E, A>.fold(crossinline e: (E) -> C, noinline f: (A) -> C): URIO<R, C> =
        map(f).recover(e)

inline fun <R, E, A, L, B> KIO<R, E, A>.bimap(crossinline f: (E) -> L, crossinline g: (A) ->B): KIO<R, L, B> = doResultMap {
    when (it) {
        is Success -> Success(g(it.value))
        is Failure -> Failure(f(it.error))
    }
}

inline fun <R, E, A> KIO<R, E, A>.filterTo(crossinline e: (A) -> E, crossinline f: (A) -> Boolean): KIO<R, E, A> = doResultMap {
    when (it) {
        is Success -> if (f(it.value)) it else Failure(e(it.value))
        is Failure -> it
    }
}

fun <R, E, A, B> ((A) -> B).lift(): (KIO<R, E, A>) -> KIO<R, E, B> = { a -> a.map(this) }

fun <R, A> KIO<R, Nothing, A>.attempt(): RIO<R, A> = Attempt(this)

fun <R, E, A> KIO<R, E, A>.provide(r: R): IO<E, A> = ProvideR(r, this)

inline fun <R, E, A1, A2, B> mapN(a: KIO<R, E, A1>, b: KIO<R, E, A2>, crossinline f: (A1, A2) -> B): KIO<R, E, B> =
        a.flatMap { r1 -> b.map { r2 -> f(r1, r2) } }

inline fun <R, E, A1, A2, A3, B> mapN(a: KIO<R, E, A1>, b: KIO<R, E, A2>, c: KIO<R, E, A3>, crossinline f: (A1, A2, A3) -> B): KIO<R, E, B> =
        a.flatMap { r1 -> b.flatMap { r2 -> c.map { r3 -> f(r1, r2, r3) } } }
