package it.msec.sparrow

import it.msec.sparrow.EvalFn.later
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("UNCHECKED_CAST")
fun <E, A1, A2, B> parMap(a: BIO<E, A1>, b: BIO<E, A2>, f: (A1, A2) -> B, ctx: CoroutineContext = EmptyCoroutineContext): BIO<List<E>, B> = later {
    runBlocking(ctx) {
        val results = listOf(a, b)
                .map { async { it.unsafeRunSync() } }
                .map { it.await() }

        if (results.all { it is Success })
            Success(f(results[0] as A1, results[1] as A2))
        else
            Failure(results.filterIsInstance<Failure<E>>().map { it.error })
    }
}

@Suppress("UNCHECKED_CAST")
fun <E, A1, A2, A3, B> parMap(a: BIO<E, A1>, b: BIO<E, A2>, c: BIO<E, A3>, f: (A1, A2, A3) -> B, ctx: CoroutineContext = EmptyCoroutineContext): BIO<List<E>, B> = later {
    runBlocking(ctx) {

        val results = listOf(a, b, c)
                .map { async { it.unsafeRunSync() } }
                .map { it.await() }

        if (results.all { it is Success })
            Success(f(results[0] as A1, results[1] as A2, results[2] as A3))
        else
            Failure(results.filterIsInstance<Failure<E>>().map { it.error })
    }
}
