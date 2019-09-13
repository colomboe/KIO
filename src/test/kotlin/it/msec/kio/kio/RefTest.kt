package it.msec.kio.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.flatMap
import it.msec.kio.map
import it.msec.kio.ref.Ref
import it.msec.kio.result.get
import it.msec.kio.runtime.unsafeRunSync
import org.junit.Test

class RefTest {

    @Test
    fun refTest() {

        val ref = Ref(33)

        val getValue = ref.get().map { it * 2 }.unsafeRunSync().get()
        val setAndGetValue = ref.set(35).flatMap { ref.get() }.unsafeRunSync().get()
        val getAndUpdate = ref.getAndUpdate { it * 2 }.unsafeRunSync().get()
        val currentValue = ref.get().unsafeRunSync().get()

        assertThat(getValue).isEqualTo(66)
        assertThat(setAndGetValue).isEqualTo(35)
        assertThat(getAndUpdate).isEqualTo(35)
        assertThat(currentValue).isEqualTo(70)

    }
}
