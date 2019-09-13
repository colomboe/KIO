package it.msec.kio

object Empty
typealias Option<A> = BIO<Empty, A>

fun <A> empty(): Option<A> = failure(Empty)

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
        flatMap { a -> if (f(a)) just(a) else failure(Empty) }

