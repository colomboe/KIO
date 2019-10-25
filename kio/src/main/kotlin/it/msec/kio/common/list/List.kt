package it.msec.kio.common.list

import it.msec.kio.KIO
import it.msec.kio.common.functions.identity
import it.msec.kio.flatMap
import it.msec.kio.justR
import it.msec.kio.map

inline fun <A, R, E, B> List<A>.traverse(f: (A) -> KIO<R, E, B>): KIO<R, E, List<B>> =
        foldRight(justR(emptyList())) { a, kio: KIO<R, E, List<B>> ->
            f(a).flatMap { b ->
                kio.map { listOf(b) + it }
            }
        }

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = traverse(::identity)
