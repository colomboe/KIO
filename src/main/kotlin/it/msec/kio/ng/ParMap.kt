package it.msec.kio.ng

import it.msec.kio.KIO
import it.msec.kio.Ko
import it.msec.kio.NgFn.laterEnv
import it.msec.kio.Ok
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, f: (A1, A2) -> B): KIO<R, List<E>, B> =
        laterEnv { env ->
            val results = listOf(a, b)
                    .map { async { it.unsafeRunSuspended(env) } }
                    .map { it.await() }

            if (results.all { it is Ok })
                Ok(f(results[0] as A1, results[1] as A2))
            else
                Ko(results.filterIsInstance<Ko<E>>().map { it.error })
        }

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, A3, B> parMap(a: KIO<R, E, A1>, b: KIO<R, E, A2>, c: KIO<R, E, A3>, f: (A1, A2, A3) -> B): KIO<R, List<E>, B> =
        laterEnv { env ->

            val results = listOf(a, b, c)
                    .map { async { it.unsafeRunSuspended(env) } }
                    .map { it.await() }

            if (results.all { it is Ok })
                Ok(f(results[0] as A1, results[1] as A2, results[2] as A3))
            else
                Ko(results.filterIsInstance<Ko<E>>().map { it.error })
        }
