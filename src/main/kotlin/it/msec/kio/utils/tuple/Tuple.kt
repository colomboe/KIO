package it.msec.kio.utils.tuple

data class T2<A, B>(val _1: A, val _2: B)
data class T3<A, B, C>(val _1: A, val _2: B, val _3: C)
data class T4<A, B, C, D>(val _1: A, val _2: B, val _3: C, val _4: D)
data class T5<A, B, C, D, E>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E)
data class T6<A, B, C, D, E, F>(val _1: A, val _2: B, val _3: C, val _4: D, val _5: E, val _6 : F)

fun <A, B> T(a: A, b: B) = T2(a, b)
fun <A, B, C> T(a: A, b: B, c: C) = T3(a, b, c)
fun <A, B, C, D> T(a: A, b: B, c: C, d: D) = T4(a, b, c, d)
fun <A, B, C, D, E> T(a: A, b: B, c: C, d: D, e: E) = T5(a, b, c, d, e)
fun <A, B, C, D, E, F> T(a: A, b: B, c: C, d: D, e: E, f: F) = T6(a, b, c, d, e, f)
