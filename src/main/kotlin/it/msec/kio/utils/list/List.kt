package it.msec.kio.utils.list

import it.msec.kio.KIO
import it.msec.kio.failureR
import it.msec.kio.internals.KIOInternals.doAccessR
import it.msec.kio.justR
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.unsafeRunSuspended
import it.msec.kio.utils.functions.identity

fun <A, R, E, B> List<A>.traverse(f: (A) -> KIO<R, E, B>): KIO<R, E, List<B>> = doAccessR { env ->
    val results = map { f(it).unsafeRunSuspended(env) }
    if (results.all { it is Success<B> }) {
        justR(results.map { (it as Success<B>).value })
    } else
        failureR(results.filterIsInstance<Failure<E>>().map { it.error }.first())
}

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = traverse(::identity)
