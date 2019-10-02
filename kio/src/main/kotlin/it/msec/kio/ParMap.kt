package it.msec.kio

import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.v2.RuntimeSuspendedV2.unsafeRunSuspended
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
inline fun <R, E, A1, A2, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, crossinline f: (A1, A2) -> B): KIO<R, List<E>, B> =
        ask<R>().flatMap { r ->
            lazySuspended<R, List<E>, B> {

                val results = listOf(a, b)
                        .map { async { unsafeRunSuspended(it, r) } }
                        .map { it.await() }

                if (results.all { it is Success })
                    Success(f((results[0] as Success<A1>).value, (results[1] as Success<A2>).value))
                else
                    Failure(results.filterIsInstance<Failure<E>>().map { it.error })
            }
        }

@Suppress("UNCHECKED_CAST")
inline fun <R, E, A1, A2, A3, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, c: KIO<R, E, A3>, crossinline f: (A1, A2, A3) -> B): KIO<R, List<E>, B> =
        ask<R>().flatMap { r ->
            lazySuspended<R, List<E>, B> {

                val results = listOf(a, b, c)
                        .map { async { unsafeRunSuspended(it, r) } }
                        .map { it.await() }

                if (results.all { it is Success })
                    Success(f((results[0] as Success<A1>).value, (results[1] as Success<A2>).value, (results[2] as Success<A3>).value))
                else
                    Failure(results.filterIsInstance<Failure<E>>().map { it.error })
            }
        }

inline fun <R, E, A, B> parMapN(vararg xs: KIO<R, E, A>, crossinline f: (List<A>) -> B): KIO<R, List<E>, B> =
        ask<R>().flatMap { r ->
            lazySuspended<R, List<E>, B> {

                val results = xs
                        .map { async { unsafeRunSuspended(it, r) } }
                        .map { it.await() }

                if (results.all { it is Success })
                    Success(f(results.filterIsInstance<Success<A>>().map { it.value }))
                else
                    Failure(results.filterIsInstance<Failure<E>>().map { it.error })
            }
        }
