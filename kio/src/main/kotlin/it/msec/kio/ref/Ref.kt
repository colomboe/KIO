package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.effect
import java.util.concurrent.atomic.AtomicReference

@Suppress("DataClassPrivateConstructor")
data class Ref<A> private constructor(private val value: AtomicReference<A>) {
    constructor(initialValue: A) : this(AtomicReference(initialValue))

    fun get(): UIO<A> = effect { value.get() }

    fun set(newValue: A): UIO<Unit> = effect { value.set(newValue) }

    fun getAndUpdate(f: (A) -> A): UIO<A> = effect { value.getAndUpdate(f) }

    fun updateAndGet(f: (A) -> A): UIO<A> = effect { value.updateAndGet(f) }

}


