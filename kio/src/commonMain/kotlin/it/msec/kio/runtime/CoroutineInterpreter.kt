package it.msec.kio.runtime

import it.msec.kio.*
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success
import kotlinx.coroutines.CoroutineScope

object CoroutineInterpreter {

    suspend fun <R, E, A> CoroutineScope.unsafeRunSuspended(kio: KIO<R, E, A>, env: R) = execute(kio, env)

    @Suppress("UNCHECKED_CAST")
    suspend fun <R, E, A> CoroutineScope.execute(k: KIO<R, E, A>, initialR: R): Result<E, A> {

        val stack = RuntimeStack()

        var r: Any? = initialR
        var current: Any = k
        while (true) {
            current = when (current) {
                is Eager<*, *, *> -> current.value
                is Lazy<*, *, *> -> try { current.valueF() } catch(t: Throwable) { Failure(t) }
                is LazySuspended<*, *, *> -> try { current.suspendedF(this) } catch(t: Throwable) { Failure(t) }
                is AskR<*, *, *> -> (current.accessF as (R) -> KIO<R, *, *>)(r as R)
                is SuccessMap<*, *, *, *> -> {
                    stack.push(successMapToF(current))
                    current.prev
                }
                is FlatMap<*, *, *, *, *> -> {
                    stack.push(current.flatMapF as RuntimeFn)
                    current.prev
                }
                is Attempt<*, *> -> current.urio
                is Result<*, *> -> {
                    val fn = stack.pop()
                    if (fn != null) fn(current) else return current as Result<E, A>
                }
                is ProvideR<*, *, *> -> {
                    val prevR = r
                    stack.push { result -> RestoreR(prevR, result) }
                    r = current.r
                    current.prev
                }
                is RestoreR<*, *, *> -> {
                    r = current.r
                    current.value
                }
                else -> throw NeverHereException
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <R, E, A ,B> successMapToF(m: SuccessMap<R, E, A, B>): RuntimeFn = {
        Eager<R, E, A>(when (it) {
            is Success<*> -> Success(m.mapF(it.value as B))
            is Failure<*> -> it as Failure<E>
        })
    }

    object NeverHereException : RuntimeException()

}
