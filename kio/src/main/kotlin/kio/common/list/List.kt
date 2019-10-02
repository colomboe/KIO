package kio.common.list

import kio.KIO
import kio.common.functions.identity
import kio.flatMap
import kio.justR
import kio.map

inline fun <A, R, E, B> List<A>.traverse(f: (A) -> KIO<R, E, B>): KIO<R, E, List<B>> =
        foldRight(justR(emptyList())) { a, kio ->
            f(a).flatMap { b ->
                kio.map { listOf(b) + it }
            }
        }

fun <R, E, A> List<KIO<R, E, A>>.sequence(): KIO<R, E, List<A>> = traverse(::identity)
