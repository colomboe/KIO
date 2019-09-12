package it.msec.kio

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
