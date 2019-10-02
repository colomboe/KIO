package it.msec.kio.internals

import it.msec.kio.*
import it.msec.kio.result.Result
import kotlinx.coroutines.CoroutineScope

object KIOInternals {

    fun <R, E, A> eager(r: Result<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: () -> Result<E, A>) =
            Lazy<R, E, A>(f)

    fun <R, E, A> lazySuspended(f: suspend CoroutineScope.() -> Result<E, A>) =
            LazySuspended<R, E, A>(f)

    fun <R, E, A> doAskR(f: (R) -> KIO<R, E, A>) =
            AskR(f)

    fun <R, E, L, A, B> KIO<R, E, A>.doMap(f: (Result<E, A>) -> Result<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.doFlatMap(f: (Result<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
            FlatMap(f, this)

}

