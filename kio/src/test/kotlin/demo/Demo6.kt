package demo

import it.msec.kio.*

fun doSomething(): IO<Int, String> = just("value")

fun printMessage(message: String): UIO<Unit> = effect { println(message) }

fun noError() {

    doSomething()
            .map { "Value length: ${it.length}" }
            .peekError { printMessage("Error: $it") }
            .flatMap { printMessage("Success: $it") }
            .swap()
            .flatMap { printMessage("Success: $it") }

    just("33").map { it.length }

//            .map { it * 2 }


}

