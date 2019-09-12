package it.msec.kio

sealed class OptionalEmpty
object Empty : OptionalEmpty()

typealias Option<A> = BIO<OptionalEmpty, A>

fun empty() = failure(Empty)
