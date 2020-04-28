package it.msec.kio.result

import it.msec.kio.Empty
import kotlinx.coroutines.CancellationException

sealed class Result<out E, out A>
data class Success<A>(val value: A) : Result<Nothing, A>()
data class Failure<E>(val error: E) : Result<E, Nothing>()
data class Cancelled(val exception: CancellationException) : Result<Nothing, Nothing>()

class UnexpectedError(t: Throwable) : RuntimeException("Unsafe code has not been correctly wrapped", t)

@Suppress(
        "UNREACHABLE_CODE",
        "IMPLICIT_NOTHING_AS_TYPE_PARAMETER",
        "ThrowableNotThrown")
fun <A> Result<Nothing, A>.get(): A =
        when (this) {
            is Success -> value
            is Failure -> throw UnexpectedError(error)
            is Cancelled -> throw exception
        }

fun <A> Result<Empty, A>.getOrNull(): A? =
        when (this) {
            is Success -> value
            else -> null
        }

fun <A> Result<Throwable, A>.getOrThrow() =
        when (this) {
            is Success -> value
            is Failure -> throw error
            is Cancelled -> throw exception
        }
