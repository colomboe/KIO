package it.msec.kio.ref

import it.msec.kio.UIO
import it.msec.kio.common.tuple.T2

expect class Ref<A> {

    fun get(): UIO<A>

    fun set(newValue: A): UIO<Unit>

    fun getAndUpdate(f: (A) -> A): UIO<A>

    fun updateAndGet(f: (A) -> A): UIO<A>

    fun <B> modify(f: (A) -> T2<A, B>): UIO<B>

    fun update(f: (A) -> A): UIO<Unit>

}
