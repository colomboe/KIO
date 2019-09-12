package it.msec.kio

typealias Try<A> = BIO<Throwable, A>

fun <A> Result<Throwable, A>.getOrThrow() =
        when (this) {
            is Success -> value
            is Failure -> throw error
        }
