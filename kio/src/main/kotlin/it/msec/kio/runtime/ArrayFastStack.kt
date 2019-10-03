package it.msec.kio.runtime

import it.msec.kio.KIO
import it.msec.kio.result.Result

typealias RuntimeFn = (Result<*, *>) -> KIO<*, *, *>
typealias RuntimeStack = ArrayFastStack<RuntimeFn>

private const val BlockSize = 8
private const val LastPossibleElementIndex = BlockSize - 1

class ArrayFastStack<F> {

    var stack: Array<Any?> = arrayOfNulls(BlockSize)
    var lastPushedElementIndex = 0

    fun push(f: F) {
        if (lastPushedElementIndex < LastPossibleElementIndex)
            stack[++lastPushedElementIndex] = f
        else {
            val newStack: Array<Any?> = arrayOfNulls(BlockSize)
            newStack[0] = stack
            newStack[1] = f
            stack = newStack
            lastPushedElementIndex = 1
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun pop(): F? =
            when {
                lastPushedElementIndex > 0 -> stack[lastPushedElementIndex--] as F
                stack[0] != null -> {
                    stack = stack[0] as Array<Any?>
                    lastPushedElementIndex = LastPossibleElementIndex
                    stack[lastPushedElementIndex--] as F
                }
                else -> null
            }

}
