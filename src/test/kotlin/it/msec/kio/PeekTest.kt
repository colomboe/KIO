package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.result.getOrThrow
import it.msec.kio.runtime.unsafeRunSync
import org.junit.Test

class PeekTest {

    @Test
    fun `peek triggers a side effect without changing the KIO success value`() {

        var sideEffectDestination = ""

        val r = just("Hello")
                .peek { unsafe { sideEffectDestination = "$it!!!" } }
                .unsafeRunSync()
                .getOrThrow()

        assertThat(r).isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("Hello!!!")
    }

    @Test
    fun `peek doesn't trigger a side effect when KIO is a failure value`() {

        var sideEffectDestination = ""

        val r = failure("Hello")
                .peek { unsafe { sideEffectDestination = "$it!!!" } }
                .unsafeRunSync()

        assertThat(r).isInstanceOf(Failure::class).transform { it.error }.isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("")
    }

    @Test
    fun `peekError triggers a side effect without changing the KIO failure value`() {

        var sideEffectDestination = ""

        val r = failure("Hello")
                .peekError { unsafe { sideEffectDestination = "$it!!!" } }
                .unsafeRunSync()

        assertThat(r).isInstanceOf(Failure::class).transform { it.error }.isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("Hello!!!")
    }

    @Test
    fun `peekError doesn't trigger a side effect when KIO is a success value`() {

        var sideEffectDestination = ""

        val r = just("Hello")
                .peekError { unsafe { sideEffectDestination = "$it!!!" } }
                .unsafeRunSync()

        assertThat(r).isInstanceOf(Success::class).transform { it.value }.isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("")
    }

    @Test
    fun `peek can access environment`() {

        val environment = "John"
        var sideEffectDestination = ""

        val r = just("Hello")
                .peek { s -> askR { env: String -> sideEffectDestination = "$s $env!" } }
                .unsafeRunSync(environment)

        assertThat(r).isInstanceOf(Success::class).transform { it.value }.isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("Hello John!")
    }

    @Test
    fun `peekError can access environment`() {

        val environment = "John"
        var sideEffectDestination = ""

        val r = failure("Hello")
                .peekError { s -> askR { env: String -> sideEffectDestination = "$s $env!" } }
                .unsafeRunSync(environment)

        assertThat(r).isInstanceOf(Failure::class).transform { it.error }.isEqualTo("Hello")
        assertThat(sideEffectDestination).isEqualTo("Hello John!")
    }
}
