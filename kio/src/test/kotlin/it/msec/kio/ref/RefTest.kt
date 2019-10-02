package it.msec.kio.ref

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.flatMap
import it.msec.kio.unsafeRunSyncAndGet
import org.junit.Test

class RefTest {

    @Test
    fun `ref provides its value`() {
        val ref = Ref(33)
        val r = ref.get().unsafeRunSyncAndGet()
        assertThat(r).isEqualTo(33)
    }

    @Test
    fun `ref value can be set`() {
        val ref = Ref(33)
        val r = ref.set(44).flatMap { ref.get() }.unsafeRunSyncAndGet()
        assertThat(r).isEqualTo(44)
    }

    @Test
    fun `ref value can be retrieved and updated atomically`() {
        val ref = Ref(33)
        val r = ref.getAndUpdate { it * 2 }.unsafeRunSyncAndGet()
        val r2 = ref.get().unsafeRunSyncAndGet()
        assertThat(r).isEqualTo(33)
        assertThat(r2).isEqualTo(66)
    }

    @Test
    fun `ref value can be updated and then retrieved atomically`() {
        val ref = Ref(33)
        val r = ref.updateAndGet { it * 2 }.unsafeRunSyncAndGet()
        assertThat(r).isEqualTo(66)
    }
}
