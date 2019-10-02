package it.msec.kio.runtime

import it.msec.kio.IO
import it.msec.kio.KIO
import it.msec.kio.UIO
import it.msec.kio.result.Result

interface KIORuntime {

    fun <A> unsafeRunSyncAndGet(kio: UIO<A>): A

    fun <E, A> unsafeRunSync(kio: IO<E, A>): Result<E, A>

    fun <R, E, A> unsafeRunSync(kio: KIO<R, E, A>, r: R): Result<E, A>

}
