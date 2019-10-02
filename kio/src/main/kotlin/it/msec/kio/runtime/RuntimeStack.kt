package it.msec.kio.runtime

import it.msec.kio.KIO
import it.msec.kio.result.Result

typealias RuntimeFn = (Result<*, *>) -> KIO<*, *, *>

private const val BlockSize = 8
private const val LastElementIndex = BlockSize - 1
private const val PopNextIndex = BlockSize - 2

class RuntimeStack {

    var stack: Array<Any?> = arrayOfNulls(BlockSize)
    var index = 0

    fun push(f: RuntimeFn) {
        if (index < LastElementIndex)
            stack[++index] = f
        else {
            val newStack: Array<Any?> = arrayOfNulls(BlockSize)
            newStack[0] = stack
            stack = newStack
            index = 0
        }
    }

    fun pop(): RuntimeFn? =
            when {
                index > 0 -> stack[index--] as RuntimeFn
                stack[0] != null -> {
                    stack = stack[0] as Array<Any?>
                    index = PopNextIndex
                    stack[LastElementIndex] as RuntimeFn
                }
                else -> null
            }

}
