package it.msec.kio.concurrent

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.map
import it.msec.kio.result.get
import it.msec.kio.runAndGetTimeMillis
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import it.msec.kio.suspended
import kotlinx.coroutines.delay
import org.junit.Test

class RaceTest {

    @Test
    fun firstWin() {

        val f1 = suspended {
            delay(500)
            println("AAA")
            33
        }

        val f2 = suspended {
            delay(3000)
            println("BBB")
            55
        }

        val raceResult = race(f1, f2,
                { it.map { i -> "First! $i" } },
                { it.map { i -> "Second! $i" } }
        )

        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult).get() }
        assertThat(output).isEqualTo("First! 33")
        assertThat(millis).isBetween(500, 1000)
    }

    @Test
    fun secondWin() {

        val f1 = suspended {
            delay(3500)
            33
        }

        val f2 = suspended {
            delay(300)
            55
        }

        val raceResult = race(f1, f2,
                { it.map { i -> "First! $i" } },
                { it.map { i -> "Second! $i" } }
        )

        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult).get() }
        assertThat(output).isEqualTo("Second! 55")
        assertThat(millis).isBetween(300, 1000)
    }
}
