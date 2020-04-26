package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success

fun <R1, R2 : R1, E1, E2, A1, A2, B, C> onFirstCompleted(a1: KIO<R1, E1, A1>,
                                                         a2: KIO<R2, E2, A2>,
                                                         f1: (Result<E1, A1>, Fiber<E2, A2>) -> KIO<R2, B, C>,
                                                         f2: (Result<E2, A2>, Fiber<E1, A1>) -> KIO<R2, B, C>): KIO<R2, B, C> =
    a1.fork()   to { fiber1 ->
    a2.fork()   to { fiber2 ->
        Race(fiber1, fiber2, f1, f2)
    }}

fun <R1, R2 : R1, E1, E2 : E1, A1, A2, B> race(a1: KIO<R1, E1, A1>,
                                               a2: KIO<R2, E2, A2>,
                                               f1: (A1) -> KIO<R2, E1, B>,
                                               f2: (A2) -> KIO<R2, E1, B>): KIO<R2, E1, B> =
        onFirstCompleted(a1, a2,
                { r, other ->
                    when (r) {
                        is Success<A1> -> other.cancelR<R2>().flatMap { eager<R2, Nothing, A1>(r) }.flatMap(f1)
                        is Failure<E1> -> other.awaitR<R2, E2, A2>().flatMap(f2)
                    }
                },
                { r, other ->
                    when (r) {
                        is Success<A2> -> other.cancelR<R2>().flatMap { eager<R2, Nothing, A2>(r) }.flatMap(f2)
                        is Failure<E2> -> other.awaitR<R2, E1, A1>().flatMap(f1)
                    }
                }
        )


fun <R1, R2 : R1, R3 : R2, E1, E2 : E1, E3 : E2, A1, A2, A3, B> race(a1: KIO<R1, E1, A1>,
                                                                     a2: KIO<R2, E2, A2>,
                                                                     a3: KIO<R3, E3, A3>,
                                                                     f1: (A1) -> KIO<R3, E1, B>,
                                                                     f2: (A2) -> KIO<R3, E1, B>,
                                                                     f3: (A3) -> KIO<R3, E1, B>): KIO<R3, E1, B> =
        race(race(a1, a2, f1, f2), a3, ::justR, f3)
