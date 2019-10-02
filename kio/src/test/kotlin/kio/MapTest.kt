package kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kio.result.Success
import org.junit.Test

class MapTest {

    @Test
    fun `map is stack safe`() {
        val iterations = 100000

        var kio = effect { 33 }
        for (i in 1..iterations) kio = kio.map { it + 1 }

        val result = kio.unsafeRunSyncAndGet()
        assertThat(result).isEqualTo(33 + iterations)
    }

    @Test
    fun `map over an eager value ends in another eager value`() {

        val eager = just(33)
        val last = eager
                .map { it + 1 }
                .map { it + 1 }
                .map { it + 1 }
                .map { it + 1 }
                .map { it + 1 }

        assertThat(last)
                .isInstanceOf(Eager::class)
                .transform { it.value }
                .isInstanceOf(Success::class)
                .transform { it.value }
                .isEqualTo(38)
    }
}
