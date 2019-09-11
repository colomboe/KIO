package it.msec.sparrow

sealed class OptionalEmpty
object Empty : OptionalEmpty()

typealias Option<A> = Eval<Result<OptionalEmpty, A>>

fun empty() = failure(Empty)
