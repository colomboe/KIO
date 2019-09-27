package it.msec.kio

import it.msec.kio.result.Result

sealed class KIO<in R, out E, out A>
data class Eager<R, E, A>(val value: Result<E, A>): KIO<R, E, A>()
data class Lazy<R, E, A>(val valueF: suspend () -> Result<E, A>): KIO<R, E, A>()
data class EnvAccess<R, E, A>(val accessF: suspend (R) -> KIO<R, E, A>): KIO<R, E, A>()
data class FlatMap<R, E, A, L, B>(val flatMapF: suspend (Result<L, B>) -> KIO<R, E, A>, val prev: KIO<R, L, B>): KIO<R, E, A>()

typealias BIO<E, A> = KIO<Any, E, A>
typealias TaskR<R, A> = KIO<R, Nothing, A>
typealias Task<A> = TaskR<Any, A>
typealias Try<A> = BIO<Throwable, A>
typealias Option<A> = BIO<Empty, A>
