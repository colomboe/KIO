package it.msec.kio.concurrent

import it.msec.kio.KIO
import it.msec.kio.common.list.sequence
import it.msec.kio.flatMap
import it.msec.kio.map
import it.msec.kio.to

@Suppress("UNCHECKED_CAST")
inline fun <R, E, A1, A2, B> parMapN(a: KIO<R, E, A1>, b: KIO<R, E, A2>, crossinline f: (A1, A2) -> B): KIO<R, E, B> =
        a.fork()                to  { fa ->
        b.fork()                to  { fb ->
        fa.awaitR<R, E, A1>()   to  { ra ->
        fb.awaitR<R, E, A2>()   map { rb ->
        f(ra, rb)
}}}}

@Suppress("UNCHECKED_CAST")
inline fun <R, E, A1, A2, A3, B> parMapN(a: KIO<R, E, A1>, b: KIO<R, E, A2>, c: KIO<R, E, A3>, crossinline f: (A1, A2, A3) -> B): KIO<R, E, B> =
        a.fork()                to  { fa ->
        b.fork()                to  { fb ->
        c.fork()                to  { fc ->
        fa.awaitR<R, E, A1>()   to  { ra ->
        fb.awaitR<R, E, A2>()   to  { rb ->
        fc.awaitR<R, E, A3>()   map { rc ->
        f(ra, rb, rc)
}}}}}}

fun <R, E, A, B> parMapN(vararg xs: KIO<R, E, A>, f: (List<A>) -> B): KIO<R, E, B> =
        xs.map { it.fork() }
                .sequence()
                .flatMap { fs -> fs
                        .map { fiber -> fiber.awaitR<R, E, A>() }
                        .sequence() }
                .map(f)
