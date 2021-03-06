package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.common.tuple.T2
import it.msec.kio.effect
import java.util.concurrent.atomic.AtomicReference

@Suppress("DataClassPrivateConstructor")
data class Ref<A> private constructor(private val value: AtomicReference<A>) {

    companion object {
        fun <A> ref(value: A): UIO<Ref<A>> =
                effect { Ref(AtomicReference(value)) }
    }

    fun get(): UIO<A> = effect { value.get() }

    fun set(newValue: A): UIO<Unit> = effect { value.set(newValue) }

    fun getAndUpdate(f: (A) -> A): UIO<A> = effect { value.getAndUpdate(f) }

    fun updateAndGet(f: (A) -> A): UIO<A> = effect { value.updateAndGet(f) }

    fun <B> modify(f: (A) -> T2<A, B>): UIO<B> {

        tailrec fun applyF(): B {
            val currentValue = value.get()
            val (updatedValue, output) = f(currentValue)
            return if (value.compareAndSet(currentValue, updatedValue)) output else applyF()
        }

        return effect { applyF() }
    }

    fun update(f: (A) -> A): UIO<Unit> = modify { a -> T2(f(a), Unit) }

}


