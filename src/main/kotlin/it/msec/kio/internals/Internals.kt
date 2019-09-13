package it.msec.kio.internals

import it.msec.kio.*
import it.msec.kio.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*

object KIOInternals {

    fun <R, E, A> eager(r: Result<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: suspend CoroutineScope.() -> Result<E, A>) =
            Lazy<R, E, A> { coroutineScope(f) }

    inline fun <R, E, A> evalAccessR(crossinline f: suspend CoroutineScope.(R) -> KIO<R, E, A>) =
            EnvAccess { r: R -> coroutineScope { f(r) } }

    inline fun <R, E, A> laterEnv(crossinline f: suspend CoroutineScope.(R) -> Result<E, A>) =
            evalAccessR<R, E, A> { r -> eager(f(r)) }

    fun <R, E, L, A, B> KIO<R, E, A>.evalMap(f: (Result<E, A>) -> Result<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.evalFlatMap(f: suspend (Result<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
            FlatMap(f, this)

    private fun explode(e: KIO<*, *, *>): Stack<KIO<*, *, *>> {
        val stack = Stack<KIO<*, *, *>>()
        stack.push(e)
        var current: KIO<*, *, *> = e
        while (current is FlatMap<*, *, *, *, *>) {
            stack.push(current.prev)
            current = current.prev
        }
        return stack
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <R, E, A> KIO<R, E, A>.execute(r: R): Result<E, A> {
        val stack = explode(this)
        var currentValue: Any? = null
        while (stack.isNotEmpty()) {
            currentValue = when (val e = stack.pop()) {
                is Eager<*, *, *> -> e.value
                is Lazy<*, *, *> -> e.valueF()
                is EnvAccess<*, *, *> -> {
                    val returnedEval = (e.accessF as suspend (R) -> KIO<*, *, *>)(r)
                    stack.addAll(explode(returnedEval))
                }
                is FlatMap<*, *, *, *, *> -> {
                    val returnedEval = (e.flatMapF as suspend (Any?) -> KIO<*, *, *>)(currentValue)
                    stack.addAll(explode(returnedEval))
                }
            }
        }
        return currentValue as Result<E, A>
    }

}

