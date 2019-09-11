package it.msec.sparrow

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class BIOTest {

    @Test
    fun happyPath() {

        val x = just(33)
                .flatMap { v -> task { v + 2 } }
                .mapT2 { v -> v * 2 }
                .flatMap { (a, b) -> task { a + b }.flatMap { task { it * 2 } } }
                .flatMap { unsafe { throw RuntimeException("bye bye") } }
                .mapError { it.message }
                .map { it.toString() }
                .recover { "Recovered $it" }
                .swap()
                .recover { "abc" }
                .unsafeRunSync()

        assertThat(x).isEqualTo(Success("abc"))
    }
}