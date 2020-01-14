package it.msec.kio.internals

import it.msec.kio.*
import it.msec.kio.common.composition.andThen
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success
import kotlinx.coroutines.CoroutineScope

const val maxStackDepthSize = 127

object KIOInternals {

    fun <R, E, A> eager(r: Result<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: () -> Result<E, A>) =
            Lazy<R, E, A>(f)

    fun <R, E, A> lazySuspended(f: suspend CoroutineScope.() -> Result<E, A>) =
            LazySuspended<R, E, A>(f)

    fun <R, E, A> doAskR(f: (R) -> KIO<R, E, A>) =
            AskR(f)

    @Suppress("UNCHECKED_CAST")
    fun <R, E, A, B> KIO<R, E, A>.doSuccessMap(f: (A) -> B): KIO<R, E, B> = when (this) {
        is SuccessMap<*, *, *, *> -> {
            if (index < maxStackDepthSize)
                SuccessMap((this.mapF as (Any?) -> A).andThen(f), this.prev as KIO<R, E, Any?>, this.index + 1)
            else
                SuccessMap(f, this as KIO<R, E, A>, 0)
        }
        is Eager<*, *, *> -> when (this.value) {
            is Success -> Eager(Success(f((this.value as Success<A>).value)))
            is Failure -> this as KIO<R, E, B>
        }
        else -> SuccessMap(f, this, 0)
    }

    fun <R, E, L, A, B> KIO<R, E, A>.doResultMap(f: (Result<E, A>) -> Result<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.doFlatMap(f: (Result<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
            FlatMap(f, this)

}

