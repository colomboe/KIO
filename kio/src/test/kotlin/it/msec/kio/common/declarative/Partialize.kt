package it.msec.kio.common.declarative

import it.msec.kio.*


fun doSomethingThatCanFail(number: String): IO<String, Int> = unsafe { number.toInt() }.mapError { "Invalid number" }

fun fortyTwo(s: String) = 42

val recover = parametrizeThis(IO<String, Int>::recover)

val prog = ::doSomethingThatCanFail then (recover with ::fortyTwo)



