package it.msec.kio.concurrent

import it.msec.kio.*
import it.msec.kio.common.coroutines.joinFirst
import it.msec.kio.concurrent.Race2Result.First
import it.msec.kio.concurrent.Race2Result.Second
import it.msec.kio.internals.KIOInternals.eager
import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Result
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.NeverHereException
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSuspended
import kotlinx.coroutines.async

sealed class Race2Result<R1, R2> {
    data class First<R1>(val winner: R1): Race2Result<R1, Nothing>()
    data class Second<R2>(val winner: R2): Race2Result<Nothing, R2>()

    fun <C> fold(f1: (R1) -> C, f2: (R2) -> C): C =
            when(this) {
                is First -> f1(winner)
                is Second -> f2(winner)
            }
}

fun <R, A1, A2, C> URIO<R, Race2Result<URIO<R, A1>, URIO<R, A2>>>.foldRace(f1: (A1) -> C, f2: (A2) -> C) =
        flatMap {
            it.fold({ k -> k.map(f1) },
                    { k -> k.map(f2) })
        }

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2> race(a1: KIO<R, E, A1>, a2: KIO<R, E, A2>): URIO<R, Race2Result<URIO<R, A1>, URIO<R, A2>>> =
        ask { r: R ->
            lazySuspended<R, Nothing, Race2Result<URIO<R, A1>, URIO<R, A2>>> {

                val deferred1 = async { unsafeRunSuspended(a1, r) }
                val deferred2 = async { unsafeRunSuspended(a2, r) }
                val winner = listOf(deferred1, deferred2).joinFirst()

                Success(when (winner) {
                    deferred1 -> First(eager<R, E, A1>(winner.getCompleted() as Result<E, A1>))
                    deferred2 -> Second(eager<R, E, A2>(winner.getCompleted() as Result<E, A2>))
                    else -> throw NeverHereException
                } as Race2Result<URIO<R, A1>, URIO<R, A2>>)
            }
        }



