package demo.Demo2

import it.msec.kio.UIO
import it.msec.kio.effect
import it.msec.kio.map
import it.msec.kio.mapN
import kotlin.random.Random

fun getRandomNumber(): UIO<Int> = effect {
    Random.nextInt()
}

fun sum(a: Int, b: Int): Int = a + b

fun example3() {
    val a: UIO<Int> = getRandomNumber()
    val b: UIO<Int> = a
    val s: UIO<Int> = mapN(a, b, ::sum)
}

fun example4() {
    val s: UIO<Int> = mapN(getRandomNumber(), getRandomNumber(), ::sum)
}

fun example5() {
    val a: UIO<Int> = getRandomNumber()
    val s: UIO<Int> = a.map { n1 ->
        val n2 = n1
        sum(n1, n2)
    }
}

/*
    () -> A   --> IO    .flatMap
    Either<L, R>        .flatMap

    () -> Either<L, R>

    (R) -> Either<L, R>

    (R) -> A

    IO + Either + Reader




 */
