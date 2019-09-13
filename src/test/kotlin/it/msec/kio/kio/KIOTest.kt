package it.msec.kio.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.*
import it.msec.kio.ng.unsafeRunSync
import org.junit.Test

class KIOTest {

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

        assertThat(x).isEqualTo(Ok("abc"))
    }

    @Test
    fun happyPathEnv() {

        data class MyEnv(val config: String = "MyLittleConfig")

        val x =
                justEnv<MyEnv, Int>(33)
                .map { it * 2 }
                .flatMapT2 { i -> askEnv { env: MyEnv -> env.config } }
                .flatMap { (i , s) -> unsafe { println(i.toString() + s) } }
                .map { "Done" }

        val result = x.unsafeRunSync(MyEnv())
        println(result)

    }

}
