package it.msec.kio.utils.list

import it.msec.kio.KIO
import it.msec.kio.core.identity
import it.msec.kio.failureEnv
import it.msec.kio.internals.KIOInternals.evalAccessR
import it.msec.kio.justEnv
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.unsafeRunSuspended

fun <A, R, E, B> List<A>.traverse(f: (A) -> KIO<R, E, B>): KIO<R, E, List<B>> = evalAccessR { env ->
    val results = map { f(it).unsafeRunSuspended(env) }
    if (results.all { it is Success<B> }) {
        justEnv(results.map { (it as Success<B>).value })
    } else
        failureEnv(results.filterIsInstance<Failure<E>>().map { it.error }.first())
}

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = traverse(::identity)
