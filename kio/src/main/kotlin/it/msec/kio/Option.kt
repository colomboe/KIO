package it.msec.kio

object Empty

fun <A> empty(): Option<A> = failure(Empty)

fun <A> Option<A>.filter(f: (A) -> Boolean): Option<A> =
        flatMap { a -> if (f(a)) just(a) else failure(Empty) }

fun <A> A?.toOption(): Option<A> = this?.let(::just) ?: empty()

fun <E, A> KIO<Any, E, A>.toOption(): Option<A> = mapError { Empty }
