package it.msec.kio


typealias Try<A> = BIO<Throwable, A>

fun <A> Rs<Throwable, A>.getOrThrow() =
        when (this) {
            is Ok -> value
            is Ko -> throw error
        }
