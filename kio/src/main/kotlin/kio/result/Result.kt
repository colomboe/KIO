package kio.result

import kio.Empty

sealed class Result<out E, out A>
data class Success<A>(val value: A) : Result<Nothing, A>()
data class Failure<E>(val error: E) : Result<E, Nothing>()

fun <A> Result<Nothing, A>.get(): A =
        when (this) {
            is Success -> value
            is Failure -> throw RuntimeException("Never here")
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
