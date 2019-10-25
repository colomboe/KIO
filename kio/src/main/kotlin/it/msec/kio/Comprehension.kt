package it.msec.kio

import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSuspended
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.reflect.KProperty

fun <R, E, A> binding(f: suspend MagicWorld<R, E>.() -> A): KIO<R, E, A> =
        ask { r: R ->
            lazySuspended<R, E, A> {
                val world = MagicWorld<R, E>(r, this)
                try {
                    Success(world.f())
                } catch (e: HiddenFailureForBindingException) {
                    e.failure as Failure<E>
                }
            }
        }

class FantoccioDelegate<A>(private val a: A) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): A = a
}

class MagicWorld<R, E>(private val r: R, private val coroutineScope: CoroutineScope) {

    suspend operator fun <A> KIO<R, E, A>.unaryPlus(): FantoccioDelegate<A> = FantoccioDelegate(this.bind())

    suspend fun <A> KIO<R, E, A>.bind(): A {
        val result = with(coroutineScope) {
            async { unsafeRunSuspended(this@bind, r) }.await()
        }
        return when (result) {
            is Success -> result.value
            is Failure -> throw HiddenFailureForBindingException(result)
        }
    }

}

class HiddenFailureForBindingException(val failure: Failure<*>) : RuntimeException()
