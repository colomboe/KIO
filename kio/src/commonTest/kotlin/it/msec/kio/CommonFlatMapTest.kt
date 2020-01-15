package it.msec.kio

import it.msec.kio.result.get
import runEffectAndAssert
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonFlatMapTest {

    @Test
    fun nested_flatMap_and_lazy_constructors() {

        fun loop(n: Int): UIO<Int> =
                if (n <= 1) effect { n }
                else loop(n - 1).flatMap { a ->
                    loop(n - 2).flatMap { b -> effect { a + b } }
                }

        val kio = loop(20)

        return runEffectAndAssert(kio, Unit) { result ->
            assertEquals(6765, result.get())
        }
    }

    @Test
    fun flatMap_is_stack_safe() {
        val iterations = 100000

        var kio = effect { 33 }
        for (i in 1..iterations) kio = kio.flatMap { effect { it + 1 } }

        runEffectAndAssert(kio, Unit) { result ->
            assertEquals(33 + iterations, result.get())
        }
    }

}
