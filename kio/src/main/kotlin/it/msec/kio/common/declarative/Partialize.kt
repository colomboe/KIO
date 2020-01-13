package it.msec.kio.common.declarative

fun <A, B, C> parametrizeThis(f: A.(B) -> C): (A, B) -> C = { a, b -> a.f(b) }
