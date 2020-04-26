package it.msec.kio.concurrent

import it.msec.kio.KIO
import it.msec.kio.common.list.sequence
import it.msec.kio.flatMap
import it.msec.kio.map
import it.msec.kio.to

fun <R1, E1, A1, R2 : R1, E2 : E1, A2, B> parMapN(a: KIO<R1, E1, A1>, b: KIO<R2, E2, A2>, f: (A1, A2) -> B): KIO<R2, E1, B> =
        a.fork()                  to  { fa ->
        b.fork()                  to  { fb ->
        fa.awaitR<R1, E1, A1>()   to  { ra ->
        fb.awaitR<R2, E2, A2>()   map { rb ->
        f(ra, rb)
}}}}

fun <R1, E1, A1, R2 : R1, E2 : E1, A2, R3 : R2, E3 : E2, A3, B> parMapN(
                                         a: KIO<R1, E1, A1>,
                                         b: KIO<R2, E2, A2>,
                                         c: KIO<R3, E3, A3>,
                                         f: (A1, A2, A3) -> B): KIO<R3, E1, B> =
        a.fork()                  to  { fa ->
        b.fork()                  to  { fb ->
        c.fork()                  to  { fc ->
        fa.awaitR<R3, E1, A1>()   to  { ra ->
        fb.awaitR<R3, E2, A2>()   to  { rb ->
        fc.awaitR<R3, E3, A3>()   map { rc ->
        f(ra, rb, rc)
}}}}}}

fun <R, E, A, B> parMapN(vararg xs: KIO<R, E, A>, f: (List<A>) -> B): KIO<R, E, B> =
        xs.map { it.fork() }
                .sequence()
                .flatMap { fs -> fs
                        .map { fiber -> fiber.awaitR<R, E, A>() }
                        .sequence() }
                .map(f)
