package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.result.get
import it.msec.kio.runtime.Runtime
import org.junit.Test

class ProvideRTest {

    @Test
    fun `R can be provided before execution`() {

        val prog = effect { 33 }
                .map { it * 2 }
                .flatMap { v -> ask { r: Int -> effect { v + r } } }
                .map { it * 2 }

        val r1 = Runtime.unsafeRunSync(prog.provide(10)).get()
        val r2 = Runtime.unsafeRunSync(prog, 10).get()

        assertThat(r1).isEqualTo(r2)
    }

    @Test
    fun `Different R types can be used`() {

        val prog = effect { 33 }
                .map { it * 2 }
                .flatMap { v -> ask { r: Int -> effect { v + r } } }
                .map { it * 2 }
                .provide(10)
                .flatMapT { askPure<String>() }
                .map { (a, b) -> "$a $b" }

        val r1 = Runtime.unsafeRunSync(prog.provide("bye")).get()
        val r2 = Runtime.unsafeRunSync(prog, "bye").get()

        assertThat(r1).isEqualTo(r2)
        println(r1)
    }

    @Test
    fun `Different R for nested instances`() {

        val prog = effect { 33 }
                .flatMap { v -> askPure { i: Int -> i + v }  }
                .flatMap { a -> askPure { s: String -> "hello $s" }.provide("string $a") }
                .provide(10)

        val r = Runtime.unsafeRunSync(prog).get()
        assertThat(r).isEqualTo("hello string 43")
    }
}
