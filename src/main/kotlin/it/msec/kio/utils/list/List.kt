package it.msec.kio.utils.list

import it.msec.kio.*
import it.msec.kio.NgFn.evalAccessR
import it.msec.kio.ng.unsafeRunSuspended

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = evalAccessR { env ->
    val results = map { it.unsafeRunSuspended(env) }
    if (results.all { it is Ok<A> }) {
        justEnv(results.map { (it as Ok<A>).value })
    } else
        failureEnv(results.filterIsInstance<Ko<E>>().map { it.error }.first())
}
