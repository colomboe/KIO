//package it.msec.kio.result
//
//sealed class Result<out E, out A>
//data class Success<A>(val value: A) : Result<Nothing, A>()
//data class Failure<E>(val error: E) : Result<E, Nothing>()
//
//fun <A> Result<Nothing, A>.get(): A =
//        when (this) {
//            is Success -> value
//            is Failure -> throw IllegalArgumentException("Impossible!")
//        }
//
