package it.msec.kio

import it.msec.kio.result.Result

sealed class KIO<in R, out E, out A>
data class Eager<R, E, A>(val value: Result<E, A>): KIO<R, E, A>()
data class Lazy<R, E, A>(val valueF: suspend () -> Result<E, A>): KIO<R, E, A>()
data class EnvAccess<R, E, A>(val accessF: suspend (R) -> KIO<R, E, A>): KIO<R, E, A>()
data class FlatMap<R, E, B, L, A>(val flatMapF: suspend (Result<E, A>) -> KIO<R, L, B>, val prev: KIO<R, E, A>): KIO<R, L, B>()

typealias BIO<E, A> = KIO<Any, E, A>
typealias EnvTask<R, A> = KIO<R, Nothing, A>
typealias Task<A> = EnvTask<Any, A>
