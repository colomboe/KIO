package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.common.coroutines.joinFirst
import it.msec.kio.concurrent.Race3Result.*
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Result
import it.msec.kio.result.Success
import it.msec.kio.runtime.CoroutineInterpreter.NeverHereException
import it.msec.kio.runtime.CoroutineInterpreter.unsafeRunSuspended
import kotlinx.coroutines.async

sealed class Race3Result<R1, R2, R3> {
    data class First<R1>(val winner: R1): Race3Result<R1, Nothing, Nothing>()
    data class Second<R2>(val winner: R2): Race3Result<Nothing, R2, Nothing>()
    data class Third<R3>(val winner: R3): Race3Result<Nothing, Nothing, R3>()

    fun <C> fold(f1: (R1) -> C, f2: (R2) -> C, f3: (R3) -> C): C =
            when(this) {
                is First -> f1(winner)
                is Second -> f2(winner)
                is Third -> f3(winner)
            }
}

fun <R, A1, A2, A3, C> URIO<R, Race3Result<URIO<R, A1>, URIO<R, A2>, URIO<R, A3>>>.foldRace(f1: (A1) -> C, f2: (A2) -> C, f3: (A3) -> C) =
        flatMap {
            it.fold({ k -> k.map(f1) },
                    { k -> k.map(f2) },
                    { k -> k.map(f3) })
        }

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, A3> race(a1: KIO<R, E, A1>, a2: KIO<R, E, A2>, a3: KIO<R, E, A3>): URIO<R, Race3Result<URIO<R, A1>, URIO<R, A2>, URIO<R, A3>>> =
        ask { r: R ->
            lazySuspended<R, Nothing, Race3Result<URIO<R, A1>, URIO<R, A2>, URIO<R, A3>>> {

                val deferred1 = async { unsafeRunSuspended(a1, r) }
                val deferred2 = async { unsafeRunSuspended(a2, r) }
                val deferred3 = async { unsafeRunSuspended(a3, r) }
                val winner = listOf(deferred1, deferred2, deferred3).joinFirst()

                Success(when (winner) {
                    deferred1 -> First(eager<R, E, A1>(winner.getCompleted() as Result<E, A1>))
                    deferred2 -> Second(eager<R, E, A2>(winner.getCompleted() as Result<E, A2>))
                    deferred3 -> Third(eager<R, E, A3>(winner.getCompleted() as Result<E, A3>))
                    else -> throw NeverHereException
                } as Race3Result<URIO<R, A1>, URIO<R, A2>, URIO<R, A3>>)
            }
        }



