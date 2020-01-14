package it.msec.kio.common.coroutines

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.selects.select

suspend fun <T, E : Deferred<T>> Iterable<E>.joinFirst(): Deferred<T> = select {
    for (deferred in this@joinFirst) {
        deferred.onAwait {
            this@joinFirst.filterNot { it == deferred }.forEach { it.cancel() }
            deferred
        }
    }
}
