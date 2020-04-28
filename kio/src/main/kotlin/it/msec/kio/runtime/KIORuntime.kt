package it.msec.kio.runtime

import it.msec.kio.*
import it.msec.kio.result.Cancelled
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success

interface KIORuntime {

    fun <A> unsafeRunSyncAndGet(kio: UIO<A>): A

    fun <E, A> unsafeRunSync(kio: IO<E, A>): Result<E, A>

    fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R): Result<E, A>

    @Suppress("UNCHECKED_CAST")
    fun <R, E, A ,B> successMapToF(m: SuccessMap<R, E, A, B>): RuntimeFn = {
        Eager<R, E, A>(when (it) {
            is Success<*> -> Success(m.mapF(it.value as B))
            is Failure<*> -> it as Failure<E>
            is Cancelled -> it
        })
    }

}
