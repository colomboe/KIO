package kio.runtime

import kio.*
import kio.result.Failure
import kio.result.Result
import kio.result.Success

interface KIORuntime {

    fun <A> unsafeRunSyncAndGet(kio: UIO<A>): A

    fun <E, A> unsafeRunSync(kio: IO<E, A>): Result<E, A>

    fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R): Result<E, A>

    @Suppress("UNCHECKED_CAST")
    fun <R, E, A ,B> successMapToF(m: SuccessMap<R, E, A, B>): RuntimeFn = {
        Eager<R, E, A>(when (it) {
            is Success<*> -> Success(m.mapF(it.value as B))
            is Failure<*> -> it as Failure<E>
        })
    }

}
