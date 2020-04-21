package demo.Constructors

import it.msec.kio.*
import kotlinx.coroutines.delay

val a = just(33)
val b = failure(33)
val c = effect { println("Hello") }
val d = unsafe { readLine()!! }
val e = suspended { delay(100) }
val f = unsafeSuspended { delay(100) }
