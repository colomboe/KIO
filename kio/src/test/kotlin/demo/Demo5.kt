package demo.Demo5

interface Service1
interface Service2
interface Service3

interface Service1And2And3 : Service1, Service2, Service3

class M<in R>

fun <R : Service1And2And3> test(): M<R> {

    val r1: M<R> = useService1()
    val r2: M<R> = useService2()
    val r3: M<R> = useService3()

    val x = useService1()
            .flatMap { useService2() }
            .flatMap { useService3() }

    return x
}

fun <R> M<R>.flatMap(f: () -> M<R>): M<R> = TODO()

fun useService1(): M<Service1> = TODO()
fun <R : Service2> useService2(): M<R> = TODO()
fun <R : Service3> useService3(): M<R> = TODO()


