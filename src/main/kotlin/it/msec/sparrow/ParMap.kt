package it.msec.sparrow

import it.msec.sparrow.EvalFn.laterEnv
import kotlinx.coroutines.async

@Suppress("UNCHECKED_CAST")
fun <R, E, A1, A2, B> parMap(a: EnvIO<R, E, A1>, b: EnvIO<R, E, A2>, f: (A1, A2) -> B): EnvIO<R, List<E>, B> =
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
fun <R, E, A1, A2, A3, B> parMap(a: EnvIO<R, E, A1>, b: EnvIO<R, E, A2>, c: EnvIO<R, E, A3>, f: (A1, A2, A3) -> B): EnvIO<R, List<E>, B> =
        laterEnv { env ->

            val results = listOf(a, b, c)
                    .map { async { it.unsafeRunSuspended(env) } }
                    .map { it.await() }

            if (results.all { it is Success })
                Success(f(results[0] as A1, results[1] as A2, results[2] as A3))
            else
                Failure(results.filterIsInstance<Failure<E>>().map { it.error })
        }
