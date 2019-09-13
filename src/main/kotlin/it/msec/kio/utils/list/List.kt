package it.msec.kio.utils.list

import it.msec.kio.KIO
import it.msec.kio.eval.EvalFn.evalAccessEnv
import it.msec.kio.failureEnv
import it.msec.kio.justEnv
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.unsafeRunSuspended

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = evalAccessEnv { env ->
    val results = map { it.unsafeRunSuspended(env) }
    if (results.all { it is Success<A> }) {
        justEnv(results.map { (it as Success<A>).value })
    } else
        failureEnv(results.filterIsInstance<Failure<E>>().map { it.error }.first())
}
