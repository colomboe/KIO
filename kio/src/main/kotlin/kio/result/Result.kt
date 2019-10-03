package kio.result

import kio.Empty

sealed class Result<out E, out A>
data class Success<A>(val value: A) : Result<Nothing, A>()
data class Failure<E>(val error: E) : Result<E, Nothing>()

class UnexpectedError(t: Throwable) : RuntimeException("Unsafe code has not been correctly wrapped", t)

@Suppress(
        "UNREACHABLE_CODE",
        "IMPLICIT_NOTHING_AS_TYPE_PARAMETER",
        "ThrowableNotThrown")
fun <A> Result<Nothing, A>.get(): A =
        when (this) {
            is Success -> value
            is Failure -> throw UnexpectedError(this.error)
        }

fun <A> Result<Empty, A>.getOrNull(): A? =
        when (this) {
            is Success -> value
            is Failure -> null
        }

fun <A> Result<Throwable, A>.getOrThrow() =
        when (this) {
            is Success -> value
            is Failure -> throw error
        }
