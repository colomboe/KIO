package kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kio.result.Failure
import org.junit.Test

class FlatMapTTest {

    @Test
    fun `mapT (2) accumulates the previous value`() {
        val (r1, r2) = just("Hello").mapT { it.length }.unsafeRunSyncAndGet()
        assertThat(r1).isEqualTo("Hello")
        assertThat(r2).isEqualTo(5)
    }

    @Test
    fun `mapT (3) accumulates the previous values`() {
        val (r1, r2, r3) = just("Hello")
                .mapT { it.length }
                .mapT { "world!!!" }
                .unsafeRunSyncAndGet()

        assertThat(r1).isEqualTo("Hello")
        assertThat(r2).isEqualTo(5)
        assertThat(r3).isEqualTo("world!!!")
    }

    @Test
    fun `flatMapT (2) accumulates the previous value on success`() {
        val (r1, r2) = just("Hello")
                .flatMapT { just(it.length) }
                .unsafeRunSyncAndGet()

        assertThat(r1).isEqualTo("Hello")
        assertThat(r2).isEqualTo(5)
    }

    @Test
    fun `flatMapT (3) accumulates the previous values on success`() {
        val (r1, r2, r3) = just("Hello")
                .flatMapT { just(it.length) }
                .flatMapT { just("world!!!") }
                .unsafeRunSyncAndGet()

        assertThat(r1).isEqualTo("Hello")
        assertThat(r2).isEqualTo(5)
        assertThat(r3).isEqualTo("world!!!")
    }

    @Test
    fun `flatMapT doesn't accumulate the previous values on failure`() {
        val r = just("Hello")
                .flatMapT { just(it.length) }
                .flatMapT { failure("Bad things") }
                .unsafeRunSync()

        assertThat(r).isInstanceOf(Failure::class).transform { it.error }.isEqualTo("Bad things")
    }
}
