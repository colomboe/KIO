package it.msec.kio

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.result.get
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import kotlinx.coroutines.delay
import org.junit.Test

class RaceTest {

    @Test
    fun name() {

        val f1 = suspended {
            delay(500)
            33
        }

        val f2 = suspended {
            delay(3000)
            55
        }

        val raceResult = race(f1, f2)
                .foldRace({ i -> "First! $i" },
                        { i -> "Second! $i" })

        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult).get() }
        assertThat(output).isEqualTo("First! 33")
        assertThat(millis).isBetween(500, 1000)
    }
}
