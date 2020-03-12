package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import org.junit.Test

class EnsureTest {

    @Test
    fun `ensure effect executed on success`() {
        var done = false

        val kio = effect { 33 }.ensuring(effect { done = true })
        unsafeRunSync(kio)

        assertThat(done).isTrue()
    }

    @Test
    fun `ensure effect executed on failure`() {
        var done = false

        val kio = failure(33).ensuring(effect { done = true })
        unsafeRunSync(kio)

        assertThat(done).isTrue()
    }

    @Test
    fun `ensure effect executed on unexpected exception`() {
        var done = false

        val kio = effect { throw RuntimeException("Something very bad") }.ensuring(effect { done = true })
        unsafeRunSync(kio)

        assertThat(done).isTrue()
    }

    @Test
    fun `ensure effect executed on handled exception`() {
        var done = false

        val kio = unsafe { throw RuntimeException("Not so unexpected") }.ensuring(effect { done = true })
        unsafeRunSync(kio)

        assertThat(done).isTrue()
    }

    @Test
    fun `ensure with environment`() {

        val env: String = "my simple environment"
        var output: String = ""

        val kio = failure(33).ensuring(askPure { e: String -> output = e })
        unsafeRunSync(kio, env)

        assertThat(output).isEqualTo(env)
    }

    @Test
    fun `ensure is stack safe`() {

        fun loop(i: Int): UIO<String> =
                if (i > 0)
                    just("hello").ensuring(loop(i - 1))
                else
                    just("done")

        val kio = just(33).ensuring(loop(3000))
        unsafeRunSync(kio)
    }

    @Test
    fun `ensure after multiple steps`() {

        var done = 0

        val kio = just(33)
                .flatMap { unsafe { it + 1 } }
                .flatMap { unsafe<Int> { throw RuntimeException("BOOOM!") } }
                .flatMap { effect { it + 3 } }
                .peek { effect { println(it) } }
                .peekError { effect { println("Error: $it"); done += 1 } }
                .ensuring(effect { done += 1 })

        unsafeRunSync(kio)

        assertThat(done).isEqualTo(2)
    }
}
