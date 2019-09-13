package it.msec.kio

import it.msec.kio.internals.KIOInternals.laterEnv
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.unsafeRunSuspended
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, f: (A1, A2) -> B): KIO<R, List<E>, B> =
        laterEnv { env ->
            val results = listOf(a, b)
                    .map { async { it.unsafeRunSuspended(env) } }
                    .map { it.await() }

            if (results.all { it is Success })
                Success(f(results[0] as A1, results[1] as A2))
            else
                Failure(results.filterIsInstance<Failure<E>>().map { it.error })
        }

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, A3, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, c: KIO<R, E, A3>, f: (A1, A2, A3) -> B): KIO<R, List<E>, B> =
        laterEnv { env ->

            val results = listOf(a, b, c)
                    .map { async { it.unsafeRunSuspended(env) } }
                    .map { it.await() }

            if (results.all { it is Success })
                Success(f(results[0] as A1, results[1] as A2, results[2] as A3))
            else
                Failure(results.filterIsInstance<Failure<E>>().map { it.error })
        }
