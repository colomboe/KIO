package it.msec.kio

import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.Success

typealias Try<A> = BIO<Throwable, A>

fun <A> Result<Throwable, A>.getOrThrow() =
        when (this) {
            is Success -> value
            is Failure -> throw error
        }
