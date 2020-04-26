package it.msec.kio

import it.msec.kio.result.Result
import kotlinx.coroutines.CoroutineScope

sealed class KIO<in R, out E, out A> {
    companion object
}
data class Eager<R, E, A>(val value: Result<E, A>): KIO<R, E, A>()
data class Lazy<R, E, A>(val valueF: () -> Result<E, A>): KIO<R, E, A>()
data class LazySuspended<R, E, A>(val suspendedF: suspend CoroutineScope.() -> Result<E, A>): KIO<R, E, A>()
data class AskR<R, E, A>(val accessF: (R) -> KIO<R, E, A>): KIO<R, E, A>()
data class FlatMap<R, E, A, L, B>(val flatMapF: (Result<L, B>) -> KIO<R, E, A>, val prev: KIO<R, L, B>): KIO<R, E, A>()
data class SuccessMap<R, E, A, B>(val mapF: (B) -> A, val prev: KIO<R, E, B>, val index: Int): KIO<R, E, A>()
data class Attempt<R, A>(val urio: KIO<R, Nothing, A>): KIO<R, Throwable, A>()
data class ProvideR<R, E, A>(val r: R, val prev: KIO<R, E, A>): IO<E, A>()
data class RestoreR<R, E, A>(val r: R, val value: Result<E, A>): KIO<R, E, A>()
data class Fork<R, E, A>(val program: KIO<R, E, A>, val env: R): KIO<R, Nothing, Fiber<E, A>>()
data class Await<R, E, A>(val fiber: Fiber<E, A>): KIO<R, E, A>()
data class Cancel<R, E, A>(val fiber: Fiber<E, A>): KIO<R, Nothing, Unit>()
data class Race<R, E1, E2, A1, A2, E, A>(val fiber1: Fiber<E1, A1>,
                                         val fiber2: Fiber<E2, A2>,
                                         val f1: (Result<E1, A1>, Fiber<E2, A2>) -> KIO<R, E, A>,
                                         val f2: (Result<E2, A2>, Fiber<E1, A1>) -> KIO<R, E, A>) : KIO<R, E, A>()

typealias IO<E, A> = KIO<Any, E, A>
typealias URIO<R, A> = KIO<R, Nothing, A>
typealias UIO<A> = URIO<Any, A>
typealias Task<A> = IO<Throwable, A>
typealias RIO<R, A> = KIO<R, Throwable, A>
typealias Option<A> = IO<Empty, A>
