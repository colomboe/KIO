package it.msec.kio.common.nullable

fun <A> A?.orElse(a: A) = this ?: a
fun <A> A?.orElse(f: () -> A) = this ?: f()
