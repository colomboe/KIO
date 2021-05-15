package it.msec.kio

import it.msec.kio.result.Result
import kotlinx.coroutines.Deferred

@JvmInline
value class Fiber<E, A>(val deferred: Deferred<Result<E, A>>)


