package it.msec.sparrow

import java.util.*

sealed class Eval<out A>
data class Eager<A>(val value: A): Eval<A>()
data class Lazy<A>(val valueF: () -> A): Eval<A>()
data class FlatMapped<IN, OUT>(val flatMapF: (IN) -> Eval<OUT>, val prev: Eval<IN>): Eval<OUT>()

object EvalFn {

    fun <A> now(a: A) = Eager(a)
    fun <A> later(f: () -> A) = Lazy(f)
    fun <IN, OUT> Eval<IN>.evalMap(f: (IN) -> OUT): Eval<OUT> = FlatMapped( { Eager(f(it)) }, this)
    fun <IN, OUT> Eval<IN>.evalFlatMap(f: (IN) -> Eval<OUT>): Eval<OUT> = FlatMapped(f, this)

    private fun explode(e: Eval<*>): Stack<Eval<*>> {
        val stack = Stack<Eval<*>>()
        stack.push(e)
        var current: Eval<*> = e
        while (current is FlatMapped<*, *>) {
            stack.push(current.prev)
            current = current.prev
        }
        return stack
    }

    @Suppress("UNCHECKED_CAST")
    fun <OUT> Eval<OUT>.execute(): OUT {
        val stack = explode(this)
        var currentValue: Any? = null
        while(stack.isNotEmpty()) {
            currentValue = when (val e = stack.pop()) {
                is Eager<*> -> e.value
                is Lazy<*> -> e.valueF()
                is FlatMapped<*, *> -> {
                    val returnedEval = (e.flatMapF as (Any?) -> Eval<*>)(currentValue)
                    stack.addAll(explode(returnedEval))
                }
            }
        }
        return currentValue as OUT
    }

}
