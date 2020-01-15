import it.msec.kio.KIO
import it.msec.kio.result.Result
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunPromise

actual fun <R, E, A> runEffectAndAssert(kio: KIO<R, E, A>, env: R, assertF: (Result<E, A>) -> Unit): dynamic =
        unsafeRunPromise(kio, env).then(assertF)
