package it.msec.kio.core

fun <A> identity(a: A) = a

inline infix fun <A, B, C> ((B) -> C).compose(crossinline f: (A) -> B): (A) -> C = { a -> this(f(a)) }
inline infix fun <A, B, C> ((A) -> B).andThen(crossinline f: (B) -> C): (A) -> C = { a -> f(this(a)) }
