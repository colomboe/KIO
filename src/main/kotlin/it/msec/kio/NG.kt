package it.msec.kio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*

sealed class Rs<out E, out A>
data class Ok<A>(val value: A) : Rs<Nothing, A>()
data class Ko<E>(val error: E) : Rs<E, Nothing>()

fun <A> Rs<Nothing, A>.get(): A =
        when (this) {
            is Ok -> value
            is Ko -> throw IllegalArgumentException("Impossible!")
        }

sealed class KIO<in R, out E, out A>
data class Eager<R, E, A>(val value: Rs<E, A>): KIO<R, E, A>()
data class Lazy<R, E, A>(val valueF: suspend () -> Rs<E, A>): KIO<R, E, A>()
data class EnvAccess<R, E, A>(val accessF: suspend (R) -> KIO<R, E, A>): KIO<R, E, A>()
data class FlatMap<R, E, B, L, A>(val flatMapF: suspend (Rs<E, A>) -> KIO<R, L, B>, val prev: KIO<R, E, A>): KIO<R, L, B>()

object NgFn {

    fun <R, E, A> eager(r: Rs<E, A>) =
            Eager<R, E, A>(r)

    fun <R, E, A> lazy(f: suspend CoroutineScope.() -> Rs<E, A>) =
            Lazy<R, E, A> { coroutineScope(f) }

    inline fun <R, E, A> evalAccessR(crossinline f: suspend CoroutineScope.(R) -> KIO<R, E, A>) =
            EnvAccess { r: R -> coroutineScope { f(r) } }

    inline fun <R, E, A> laterEnv(crossinline f: suspend CoroutineScope.(R) -> Rs<E, A>) =
            evalAccessR<R, E, A> { r -> eager(f(r)) }

    fun <R, E, L, A, B> KIO<R, E, A>.evalMap(f: (Rs<E, A>) -> Rs<L, B>): KIO<R, L, B> =
            FlatMap({ Eager<R, L, B>(f(it)) }, this)

    fun <R, E, L, A, B> KIO<R, E, A>.evalFlatMap(f: suspend (Rs<E, A>) -> KIO<R, L, B>): KIO<R, L, B> =
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
    suspend fun <R, E, A> KIO<R, E, A>.execute(r: R): Rs<E, A> {
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
        return currentValue as Rs<E, A>
    }

}

