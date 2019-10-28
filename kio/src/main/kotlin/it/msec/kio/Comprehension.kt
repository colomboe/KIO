package it.msec.kio

import it.msec.kio.internals.KIOInternals.lazySuspended
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSuspended
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlin.reflect.KProperty

operator fun <R, E, A, B> KIO<R, E, A>.plus(that: KIO<R, E, B>): KIO<R, E, B> = this.flatMap { that }
inline infix fun <R, E, A, B> KIO<R, E, A>.to(crossinline f: (a: A) -> KIO<R, E, B>): KIO<R, E, B> = this.flatMap(f)
inline operator fun <R, E, A, B> KIO<R, E, A>.plus(crossinline f: (a: A) -> KIO<R, E, B>): KIO<R, E, B> = this.flatMap(f)

// -- OLD IMPLEMENTATION --

@Deprecated("This syntax has been deprecated, please use the `+` and `to` syntax.")
@Suppress("UNCHECKED_CAST")
fun <R, E, A> binding(f: suspend BindingContext<R, E>.() -> A): KIO<R, E, A> =
        ask { r: R ->
            lazySuspended<R, E, A> {
                val world = BindingContext<R, E>(r, this)
                try {
                    Success(world.f())
                } catch (e: HiddenFailureForBindingException) {
                    e.failure as Failure<E>
                }
            }
        }

class BindingDelegate<A>(private val a: A) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): A = a
}

class BindingContext<R, E>(private val r: R, private val coroutineScope: CoroutineScope) {

    suspend operator fun <A> KIO<R, E, A>.unaryPlus(): BindingDelegate<A> = BindingDelegate(this.bind())

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
