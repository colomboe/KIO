package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.delay
import java.util.concurrent.atomic.AtomicReference

@Suppress("DataClassPrivateConstructor")
data class Ref<A> private constructor(private val value: AtomicReference<A>) {
    constructor(initialValue: A) : this(AtomicReference(initialValue))

    fun get(): UIO<A> = delay { value.get() }

    fun set(newValue: A): UIO<Unit> = delay { value.set(newValue) }

    fun getAndUpdate(f: (A) -> A): UIO<A> = delay { value.getAndUpdate(f) }

    fun updateAndGet(f: (A) -> A): UIO<A> = delay { value.updateAndGet(f) }

}


