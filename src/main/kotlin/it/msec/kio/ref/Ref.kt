package it.msec.kio.ref

import it.msec.kio.Task
import it.msec.kio.task
import java.util.concurrent.atomic.AtomicReference

data class Ref<A> private constructor(private val value: AtomicReference<A>) {
    constructor(initialValue: A) : this(AtomicReference(initialValue))

    fun get(): Task<A> = task { value.get() }

    fun set(newValue: A): Task<Unit> = task { value.set(newValue) }

    fun getAndUpdate(f: (A) -> A): Task<A> = task { value.getAndUpdate(f) }

}


