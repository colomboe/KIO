package it.msec.kio

operator fun <R, E, A, B> KIO<R, E, A>.plus(that: KIO<R, E, B>): KIO<R, E, B> = this.flatMap { that }

inline operator fun <R, E, A, B> KIO<R, E, A>.plus(crossinline f: (a: A) -> KIO<R, E, B>): KIO<R, E, B> = this.flatMap(f)

inline infix fun <R, E, A, B> KIO<R, E, A>.to(crossinline f: (a: A) -> KIO<R, E, B>): KIO<R, E, B> = this.flatMap(f)

inline infix fun <R, E, A, B> A.set(crossinline f: (a: A) -> KIO<R, E, B>): KIO<R, E, B> = f(this)
