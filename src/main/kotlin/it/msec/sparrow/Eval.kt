package it.msec.sparrow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import java.util.*

sealed class Eval<out ENV, out OUT>
data class Eager<OUT>(val value: OUT): Eval<Nothing, OUT>()
data class Lazy<OUT>(val valueF: suspend () -> OUT): Eval<Nothing, OUT>()
data class EnvAccessed<ENV, OUT>(val accessF: suspend (ENV) -> Eval<ENV, OUT>): Eval<ENV, OUT>()
data class FlatMapped<ENV, IN, OUT>(val flatMapF: suspend (IN) -> Eval<ENV, OUT>, val prev: Eval<ENV, IN>): Eval<ENV, OUT>()

object EvalFn {

    fun <OUT> eager(a: OUT) = Eager(a)
    fun <OUT> lazy(f: suspend CoroutineScope.() -> OUT) = Lazy { coroutineScope(f) }
    fun <ENV, OUT> evalAccessEnv(f: suspend CoroutineScope.(ENV) -> Eval<ENV, OUT>) = EnvAccessed { env: ENV -> coroutineScope { f(env) } }
    fun <ENV, OUT> laterEnv(f: suspend CoroutineScope.(ENV) -> OUT) = evalAccessEnv<ENV, OUT> { env -> lazy { f(env) } }

    fun <ENV, IN, OUT> Eval<ENV, IN>.evalMap(f: (IN) -> OUT): Eval<ENV, OUT> = FlatMapped( { Eager(f(it)) }, this)
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
                is Eager<*> -> e.value
                is Lazy<*> -> e.valueF()
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

