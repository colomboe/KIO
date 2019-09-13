package it.msec.kio.kio

import it.msec.kio.Option
import it.msec.kio.empty
import it.msec.kio.flatMap
import it.msec.kio.just
import it.msec.kio.ng.unsafeRunSync
import org.junit.Test

class OptionTest {

    @Test
    fun optionTest() {

        val option1: Option<Int> = just(33)
        val option2: Option<Int> = empty()

        val x= option1.flatMap { option2 }.unsafeRunSync()

        println(x)
    }
}
