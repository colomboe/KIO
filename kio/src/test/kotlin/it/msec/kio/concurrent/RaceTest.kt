package it.msec.kio.concurrent

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import it.msec.kio.*
import it.msec.kio.result.Failure
import it.msec.kio.result.Success
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Test

class RaceTest {

    var counter = 0

    fun <A> waitAndSucceed(ms: Long, v: A): UIO<A> = suspended { delay(ms); counter++; v }
    fun <E> waitAndFail(ms: Long, e: E): IO<E, Nothing> = suspended { delay(ms); counter++ }.flatMap { failure(e); }
    fun waitAndThrow(ms: Long, s: String): UIO<Nothing> = suspended { delay(ms); counter++ }.flatMap { effect { throw RuntimeException(s) } }

    @Before
    fun setUp() {
        counter = 0
    }

    @Test
    fun `first ends with success`() {

        val k1 = waitAndSucceed(500, 33)
        val k2 = waitAndSucceed(2000, 44)

        val raceResult = race(k1, k2, ::just, ::just)

        runAndAssertThat(raceResult, 33, 500L to 1000L, 1)
    }

    @Test
    fun `first ends with failure`() {

        val k1 = waitAndFail(500, "BOOOM!!!")
        val k2 = waitAndSucceed(2000, 44)

        val raceResult = race(k1, k2, ::just, ::just)

        runAndAssertThat(raceResult, 44, 2000L to 2500L, 2)
    }

    @Test
    fun `second ends with success`() {

        val k1 = waitAndSucceed(2000, 44)
        val k2 = waitAndSucceed(500, 33)

        val raceResult = race(k1, k2, ::just, ::just)

        runAndAssertThat(raceResult, 33, 500L to 1000L, 1)
    }

    @Test
    fun `second ends with failure`() {

        val k1 = waitAndSucceed(2000, 44)
        val k2 = waitAndFail(500, "BOOOM!!!")

        val raceResult = race(k1, k2, ::just, ::just)

        runAndAssertThat(raceResult, 44, 2000L to 2500L, 2)
    }

    @Test
    fun `both end with failure`() {

        val k1: IO<String, Int> = waitAndFail(500, "BOOOM!!!")
        val k2: IO<String, Int> = waitAndFail(2000, "BOOOM2!!!")

        val raceResult = race(k1, k2, ::just, ::just)

        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult) }
        assertThat((output as Failure).error).isEqualTo("BOOOM2!!!")
        assertThat(millis).isBetween(2000, 2500)
        assertThat(counter).isEqualTo(2)
    }

    @Test
    fun `first ends with unexpected exception`() {

        val k1 = waitAndThrow(500, "Throw BOOOM!!!")
        val k2 = waitAndSucceed(2000, 44)

        val raceResult = race(k1, k2, ::just, ::just)

        runAndAssertThat(raceResult, 44, 2000L to 2500L, 2)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `both end with an unexpected exception`() {

        val k1: UIO<Int> = waitAndThrow(500, "Throw BOOOM!!!")
        val k2: UIO<Int> = waitAndThrow(2000, "Throw BOOOM2!!!")

        val raceResult: UIO<Int> = race(k1, k2, ::just, ::just)

        val (output, _) = runAndGetTimeMillis { unsafeRunSync(raceResult) }
        assertThat((output as Failure<RuntimeException>).error.message).isEqualTo("Throw BOOOM2!!!")
    }

    @Test
    fun `both end with an unexpected exception but with attempt`() {

        val k1: UIO<Int> = waitAndThrow(500, "Throw BOOOM!!!")
        val k2: UIO<Int> = waitAndThrow(2000, "Throw BOOOM2!!!")

        val raceResult: Task<Int> = race(k1, k2, ::just, ::just).attempt()

        val (output, _) = runAndGetTimeMillis { unsafeRunSync(raceResult) }
        assertThat((output as Failure).error.message).isEqualTo("Throw BOOOM2!!!")
    }

    @Test
    fun `successful race with three`() {

        val k1: IO<String, Int> = waitAndFail(500, "BOOOM!!!")
        val k2: IO<String, Int> = waitAndFail(1000, "BOOOM2!!!")
        val k3: IO<String, Int> = waitAndSucceed(1500, 33)

        val raceResult: IO<String, Int> = race(k1, k2, k3, ::just, ::just, ::just)

        runAndAssertThat(raceResult, 33, 1500L to 2000L, 3)
    }

    @Test
    fun `race with three all failing`() {

        val k1: IO<String, Int> = waitAndFail(500, "BOOOM!!!")
        val k2: IO<String, Int> = waitAndFail(1500, "BOOOM2!!!")
        val k3: IO<String, Int> = waitAndFail(1000, "BOOOM3!!!")

        val raceResult: IO<String, Int> = race(k1, k2, k3, ::just, ::just, ::just)

        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult) }
        assertThat((output as Failure).error).isEqualTo("BOOOM2!!!")
        assertThat(millis).isBetween(1500, 2000)
        assertThat(counter).isEqualTo(3)
    }

    @Test
    fun `race with three with cancellation`() {

        val k1: IO<String, Int> = waitAndSucceed(500, 33)
        val k2: IO<String, Int> = waitAndFail(1500, "BOOOM2!!!")
        val k3: IO<String, Int> = waitAndFail(1000, "BOOOM3!!!")

        val raceResult: IO<String, Int> = race(k1, k2, k3, ::just, ::just, ::just)

        runAndAssertThat(raceResult, 33, 500L to 1000L, 1)
    }

    private fun runAndAssertThat(raceResult: KIO<Any, String, Int>, expectedSuccessValue: Int, expectedDuration: Pair<Long, Long>, expectedCounter: Int) {
        val (output, millis) = runAndGetTimeMillis { unsafeRunSync(raceResult) }
        assertThat((output as Success).value).isEqualTo(expectedSuccessValue)
        assertThat(millis).isBetween(expectedDuration.first, expectedDuration.second)
        assertThat(counter).isEqualTo(expectedCounter)
    }

}
