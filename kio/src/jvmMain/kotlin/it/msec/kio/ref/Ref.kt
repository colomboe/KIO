package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.common.tuple.T2
import it.msec.kio.effect
import java.util.concurrent.atomic.AtomicReference

@Suppress("DataClassPrivateConstructor")
actual class Ref<A> private constructor(private val value: AtomicReference<A>) {
    constructor(initialValue: A) : this(AtomicReference(initialValue))

    actual fun get(): UIO<A> = effect { value.get() }

    actual fun set(newValue: A): UIO<Unit> = effect { value.set(newValue) }

    actual fun getAndUpdate(f: (A) -> A): UIO<A> = effect { value.getAndUpdate(f) }

    actual fun updateAndGet(f: (A) -> A): UIO<A> = effect { value.updateAndGet(f) }

    actual fun <B> modify(f: (A) -> T2<A, B>): UIO<B> {

        tailrec fun applyF(): B {
            val currentValue = value.get()
            val (updatedValue, output) = f(currentValue)
            return if (value.compareAndSet(currentValue, updatedValue)) output else applyF()
        }

        return effect { applyF() }
    }

    actual fun update(f: (A) -> A): UIO<Unit> = modify { a -> T2(f(a), Unit) }
}
