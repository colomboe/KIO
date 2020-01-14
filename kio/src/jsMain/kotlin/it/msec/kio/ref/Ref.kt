package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.common.tuple.T2
import it.msec.kio.effect
import it.msec.kio.just

actual class Ref<A> constructor(private var value: A) {

    actual fun get(): UIO<A> = just(value)

    actual fun set(newValue: A): UIO<Unit> = effect { value = newValue }

    actual fun getAndUpdate(f: (A) -> A): UIO<A> = effect {
        val oldValue = value
        value = f(value)
        oldValue
    }

    actual fun updateAndGet(f: (A) -> A): UIO<A> = effect {
        value = f(value)
        value
    }

    actual fun <B> modify(f: (A) -> T2<A, B>): UIO<B> {

        tailrec fun applyF(): B {
            val currentValue = value
            val (updatedValue, output) = f(currentValue)
            return if (value == currentValue) {
                value = updatedValue
                output
            }
            else
                applyF()
        }

        return effect { applyF() }
    }

    actual fun update(f: (A) -> A): UIO<Unit> = modify { a -> T2(f(a), Unit) }
}
