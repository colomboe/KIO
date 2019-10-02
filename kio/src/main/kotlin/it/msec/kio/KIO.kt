package it.msec.kio

import it.msec.kio.result.Result
import kotlinx.coroutines.CoroutineScope

sealed class KIO<in R, out E, out A>
data class Eager<R, E, A>(val value: Result<E, A>): KIO<R, E, A>()
data class Lazy<R, E, A>(val valueF: () -> Result<E, A>): KIO<R, E, A>()
data class LazySuspended<R, E, A>(val suspendedF: suspend CoroutineScope.() -> Result<E, A>): KIO<R, E, A>()
data class EnvAccess<R, E, A>(val accessF: (R) -> KIO<R, E, A>): KIO<R, E, A>()
data class FlatMap<R, E, A, L, B>(val flatMapF: (Result<L, B>) -> KIO<R, E, A>, val prev: KIO<R, L, B>): KIO<R, E, A>()

typealias IO<E, A> = KIO<Any, E, A>
typealias URIO<R, A> = KIO<R, Nothing, A>
typealias UIO<A> = URIO<Any, A>
typealias Task<A> = IO<Throwable, A>
typealias RIO<R, A> = KIO<R, Throwable, A>
typealias Option<A> = IO<Empty, A>
