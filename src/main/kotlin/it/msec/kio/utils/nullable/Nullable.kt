package it.msec.kio.utils.nullable

fun <A, B> A?.then(f: (A) -> B) = this?.let(f)
fun <A> A?.orElse(a: A) = this ?: a
fun <A> A?.orElse(f: () -> A) = this ?: f()
