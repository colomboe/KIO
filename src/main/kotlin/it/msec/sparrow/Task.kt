package it.msec.sparrow

import it.msec.sparrow.EvalFn.later

typealias Task<A> = Eval<Success<A>>

fun <A> task(f: () -> A): Task<A> = later { Success(f()) }

fun <A> Result<Nothing, A>.get(): A =
        when (this) {
            is Success -> value
            is Failure -> throw IllegalArgumentException("Impossible!")
        }
