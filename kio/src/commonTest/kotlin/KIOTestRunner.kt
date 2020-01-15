import it.msec.kio.KIO
import it.msec.kio.result.Result

expect fun <R, E, A> runEffectAndAssert(kio: KIO<R, E, A>, env: R, assertF: (Result<E, A>) -> Unit): Unit
