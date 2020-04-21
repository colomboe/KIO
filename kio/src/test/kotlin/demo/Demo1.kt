package demo.Demo1

import kotlin.random.Random

fun getNumber(): Int {
    println("ciao")
    return 33
}

fun getRandomNumber(): Int = Random.nextInt()

fun sum(a: Int, b: Int): Int = a + b

fun example1() {
    val a = getNumber()
    val b = a
    val s = sum(a, b)
}

fun example2() {
    val s = sum(getNumber(), getNumber())
}

fun example3() {
    val a = getRandomNumber()
    val b = a
    val s = sum(a, b)
}

fun example4() {
    val s = sum(getRandomNumber(), getRandomNumber())
}

