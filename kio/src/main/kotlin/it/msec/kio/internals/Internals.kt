package it.msec.kio.internals

import it.msec.kio.*
import it.msec.kio.result.Result
import it.msec.kio.runtime.v2.RuntimeV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

object KIOInternals {

    fun <R, E, A> eager(r: Result<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: suspend CoroutineScope.() -> Result<E, A>) =
            Lazy<R, E, A> { coroutineScope(f) }

    inline fun <R, E, A> doAccessR(crossinline f: suspend CoroutineScope.(R) -> KIO<R, E, A>) =
            EnvAccess { r: R -> coroutineScope { f(r) } }

    inline fun <R, E, A> laterEnv(crossinline f: suspend CoroutineScope.(R) -> Result<E, A>) =
            doAccessR<R, E, A> { r -> eager(f(r)) }

    fun <R, E, L, A, B> KIO<R, E, A>.doMap(f: (Result<E, A>) -> Result<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.doFlatMap(f: suspend (Result<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
            FlatMap(f, this)

    suspend fun <R, E, A> KIO<R, E, A>.execute(r: R): Result<E, A> =
            RuntimeV2.execute(this, r)

}

