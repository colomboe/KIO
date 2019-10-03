package it.msec.kio.runtime

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class ArrayFastStackTest {

    @Test
    fun `stack stress test`() {

        val stack = ArrayFastStack<Int>()

        for(i in 1..1000) stack.push(i)

        for (i in 1000 downTo 1) {
            val e = stack.pop()
            assertThat(e).isEqualTo(i)
        }

        assertThat(stack.pop()).isNull()

    }

}
