package it.msec.kio.common.declarative

import it.msec.kio.*

@JvmName("pureThenPure")
inline infix fun <A, B, C> ((A) -> B).then(crossinline f: (B) -> C): (A) -> UIO<C> = { effect { f(this(it)) } }

@JvmName("pureThenEff")
inline infix fun <R, E, A, B, C> ((A) -> B).then(crossinline f: (B) -> KIO<R, E, C>): (A) -> KIO<R, E, C> = { a -> f(this(a)) }

@JvmName("effThenEff")
inline infix fun <R, E, A, B, C> ((A) -> KIO<R, E, B>).then(crossinline f: (B) -> KIO<R, E, C>): (A) -> KIO<R, E, C> = { this(it).flatMap(f) }

@JvmName("effThenPure")
inline infix fun <R, E, A, B, C> ((A) -> KIO<R, E, B>).then(noinline f: (B) -> C): (A) -> KIO<R, E, C> = { this(it).map(f) }

@JvmName("mapThen")
inline infix fun <R, E, A, B, C> ((KIO<R, E, A>) -> KIO<R, E, B>).then(noinline f: (B) -> C): (KIO<R, E, A>) -> KIO<R, E, C> = { this(it).map(f) }

@JvmName("flatMapThen")
inline infix fun <R, E, A, B, C> ((KIO<R, E, A>) -> KIO<R, E, B>).then(crossinline f: (B) -> KIO<R, E, C>): (KIO<R, E, A>) -> KIO<R, E, C> = { this(it).flatMap(f) }

@JvmName("manageEffectThen")
inline infix fun <R, E, L, A, B, C> ((A) -> KIO<R, E, B>).then(crossinline f: (KIO<R, E, B>) -> KIO<R, L, C>): (A) -> KIO<R, L, C> = { f(this(it)) }

@JvmName("withPure")
inline infix fun <R, E, A, B, C> ((A, B) -> C).with(crossinline f: () -> KIO<R, E, B>): (A) -> KIO<R, E, C> =
        { a -> f().map { b -> this(a, b) } }

@JvmName("withPureAndValue")
inline infix fun <R, E, A, B, C> ((A, B) -> C).with(b: B): (A) -> KIO<R, E, C> =
        { a -> justR(this(a, b)) }

@JvmName("withEff")
inline infix fun <R, E, A, B, C> ((A, B) -> KIO<R, E, C>).with(crossinline f: () -> KIO<R, E, B>): (A) -> KIO<R, E, C> =
        { a -> f().flatMap { b -> this(a, b) } }

@JvmName("withEffAndValue")
inline infix fun <R, E, A, B, C> ((A, B) -> KIO<R, E, C>).with(b: B): (A) -> KIO<R, E, C> =
        { a -> this(a, b) }

@JvmName("withPureAndParam")
inline infix fun <R, E, A, B, C> ((A, B) -> C).with(crossinline f: (A) -> KIO<R, E, B>): (A) -> KIO<R, E, C> =
        { a -> f(a).map { b -> this(a, b) } }

@JvmName("withEffAndParam")
inline infix fun <R, E, A, B, C> ((A, B) -> KIO<R, E, C>).with(crossinline f: (A) -> KIO<R, E, B>): (A) -> KIO<R, E, C> =
        { a -> f(a).flatMap { b -> this(a, b) } }

inline infix fun <R, E, A, B, C> ((A) -> KIO<R, E, B>).thenExecute(crossinline f: (B) -> KIO<R, E, C>): (A) -> KIO<R, E, B> =
        { a -> this(a).peek(f) }

inline infix fun <R, E, L, A, B> ((A) -> KIO<R, E, B>).or(crossinline f: (E) -> KIO<R, L, B>): (A) -> KIO<R, L, B> =
        { a -> this(a).tryRecover(f) }
