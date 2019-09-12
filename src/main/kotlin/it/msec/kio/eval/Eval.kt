package it.msec.kio.eval

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*

sealed class Eval<in ENV, out OUT>
data class Eager<ENV, OUT>(val value: OUT): Eval<ENV, OUT>()
data class Lazy<ENV, OUT>(val valueF: suspend () -> OUT): Eval<ENV, OUT>()
data class EnvAccessed<ENV, OUT>(val accessF: suspend (ENV) -> Eval<ENV, OUT>): Eval<ENV, OUT>()
data class FlatMapped<ENV, IN, OUT>(val flatMapF: suspend (IN) -> Eval<ENV, OUT>, val prev: Eval<ENV, IN>): Eval<ENV, OUT>()

object EvalFn {

    fun <ENV, OUT> eager(a: OUT) = Eager<ENV, OUT>(a)
    fun <ENV, OUT> lazy(f: suspend CoroutineScope.() -> OUT) = Lazy<ENV, OUT> { coroutineScope(f) }
    fun <ENV, OUT> evalAccessEnv(f: suspend CoroutineScope.(ENV) -> Eval<ENV, OUT>) = EnvAccessed { env: ENV -> coroutineScope { f(env) } }
    fun <ENV, OUT> laterEnv(f: suspend CoroutineScope.(ENV) -> OUT) = evalAccessEnv<ENV, OUT> { env -> eager(f(env)) }

    fun <ENV, IN, OUT> Eval<ENV, IN>.evalMap(f: (IN) -> OUT): Eval<ENV, OUT> = FlatMapped({ Eager<ENV, OUT>(f(it)) }, this)
    fun <ENV, IN, OUT> Eval<ENV, IN>.evalFlatMap(f: suspend (IN) -> Eval<ENV, OUT>): Eval<ENV, OUT> = FlatMapped(f, this)

    private fun explode(e: Eval<*, *>): Stack<Eval<*, *>> {
        val stack = Stack<Eval<*, *>>()
        stack.push(e)
        var current: Eval<*, *> = e
        while (current is FlatMapped<*, *, *>) {
            stack.push(current.prev)
            current = current.prev
        }
        return stack
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <ENV, OUT> Eval<ENV, OUT>.execute(env: ENV): OUT {
        val stack = explode(this)
        var currentValue: Any? = null
        while (stack.isNotEmpty()) {
            currentValue = when (val e = stack.pop()) {
                is Eager<*, *> -> e.value
                is Lazy<*, *> -> e.valueF()
                is EnvAccessed<*, *> -> {
                    val returnedEval = (e.accessF as suspend (ENV) -> Eval<*, *>)(env)
                    stack.addAll(explode(returnedEval))
                }
                is FlatMapped<*, *, *> -> {
                    val returnedEval = (e.flatMapF as suspend (Any?) -> Eval<*, *>)(currentValue)
                    stack.addAll(explode(returnedEval))
                }
            }
        }
        return currentValue as OUT
    }

}

