package it.msec.kio.internals

import it.msec.kio.*
import it.msec.kio.result.Result

object KIOInternals {

    fun <R, E, A> eager(r: Result<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: () -> Result<E, A>) =
            Lazy<R, E, A>(f)

    fun <R, E, A> lazySuspended(f: suspend () -> Result<E, A>) =
            LazySuspended<R, E, A>(f)

    fun <R, E, A> doAccessR(f: (R) -> KIO<R, E, A>) =
            EnvAccess(f)

//    inline fun <R, E, A> laterEnv(crossinline f: suspend CoroutineScope.(R) -> Result<E, A>) =
//            doAccessR<R, E, A> { r -> eager(f(r)) }

    fun <R, E, L, A, B> KIO<R, E, A>.doMap(f: (Result<E, A>) -> Result<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.doFlatMap(f: (Result<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
            FlatMap(f, this)

}

