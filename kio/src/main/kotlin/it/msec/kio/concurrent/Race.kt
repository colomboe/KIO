package it.msec.kio.concurrent

import it.msec.kio.IO
import it.msec.kio.KIO
import it.msec.kio.Race
import it.msec.kio.result.Result
import it.msec.kio.result.toEffect
import it.msec.kio.to

fun <R1, R2 : R1, E1, E2, A1, A2, L, R> raceWithResult(a1: KIO<R1, E1, A1>,
                                                       a2: KIO<R2, E2, A2>,
                                                       f1: (Result<E1, A1>) -> KIO<R2, L, R>,
                                                       f2: (Result<E2, A2>) -> KIO<R2, L, R>): KIO<R2, L, R> =
    a1.fork()   to { d1 ->
    a2.fork()   to { d2 ->
        Race(d1, d2, f1, f2)
    }}

fun <R1, R2 : R1, E1, E2, A1, A2, L, R> race(a1: KIO<R1, E1, A1>,
                                             a2: KIO<R2, E2, A2>,
                                             f1: (IO<E1, A1>) -> KIO<R2, L, R>,
                                             f2: (IO<E2, A2>) -> KIO<R2, L, R>): KIO<R2, L, R> =
        raceWithResult(a1, a2, { f1(toEffect(it)) }, { f2(toEffect(it)) })

fun <R1, R2 : R1, R3 : R2, E1, E2, E3, A1, A2, A3, C, D> race(a1: KIO<R1, E1, A1>,
                                                              a2: KIO<R2, E2, A2>,
                                                              a3: KIO<R3, E3, A3>,
                                                              f1: (Result<E1, A1>) -> KIO<R3, C, D>,
                                                              f2: (Result<E2, A2>) -> KIO<R3, C, D>,
                                                              f3: (Result<E3, A3>) -> KIO<R3, C, D>): KIO<R3, C, D> =
        raceWithResult(raceWithResult(a1, a2, f1, f2), a3, ::toEffect, f3)

@JvmName("raceWithIO")
fun <R1, R2 : R1, R3 : R2, E1, E2, E3, A1, A2, A3, C, D> race(a1: KIO<R1, E1, A1>,
                                                              a2: KIO<R2, E2, A2>,
                                                              a3: KIO<R3, E3, A3>,
                                                              f1: (IO<E1, A1>) -> KIO<R3, C, D>,
                                                              f2: (IO<E2, A2>) -> KIO<R3, C, D>,
                                                              f3: (IO<E3, A3>) -> KIO<R3, C, D>): KIO<R3, C, D> =
        raceWithResult(race(a1, a2, f1, f2), a3, ::toEffect, { f3(toEffect(it)) })
