package it.msec.sparrow

sealed class OptionalEmpty
object Empty : OptionalEmpty()

typealias Option<A> = BIO<OptionalEmpty, A>

fun empty() = failure(Empty)
