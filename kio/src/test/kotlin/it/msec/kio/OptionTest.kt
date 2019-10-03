package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import it.msec.kio.result.getOrNull
import org.junit.Test

class OptionTest {

    @Test
    fun `optional empty constructor works as expected`() {
        val e: Option<Int> = empty()
        val r: Int? = e.unsafeRunSync().getOrNull()
        assertThat(r).isNull()
    }

    @Test
    fun `filtering on optional when condition is true`() {
        val o: String? = just("Hello").filter { it.length == 5 }.unsafeRunSync().getOrNull()
        assertThat(o).isEqualTo("Hello")
    }

    @Test
    fun `filtering on optional when condition is false`() {
        val o: String? = just("Hello").filter { it.length != 5 }.unsafeRunSync().getOrNull()
        assertThat(o).isNull()
    }

    @Test
    fun `nullable to Option when null`() {
        val nullable: Int? = null
        val o: Int? = nullable.toOption().unsafeRunSync().getOrNull()
        assertThat(o).isNull()
    }

    @Test
    fun `nullable to Option when not null`() {
        val nullable: Int? = 33
        val o: Int? = nullable.toOption().unsafeRunSync().getOrNull()
        assertThat(o).isEqualTo(33)
    }

    @Test
    fun `effect to Option when failure`() {
        val e: IO<String, Int> = failure("MyError")
        val o: Int? = e.toOption().unsafeRunSync().getOrNull()
        assertThat(o).isNull()
    }

    @Test
    fun `effect to Option when success`() {
        val e: IO<String, Int> = just(33)
        val o: Int? = e.toOption().unsafeRunSync().getOrNull()
        assertThat(o).isEqualTo(33)
    }
}
