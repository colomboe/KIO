import it.msec.kio.KIO
import it.msec.kio.result.Result
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync

actual fun <R, E, A> runEffectAndAssert(kio: KIO<R, E, A>, env: R, assertF: (Result<E, A>) -> Unit): Unit {
    val result = unsafeRunSync(kio, env)
    assertF(result)
}
