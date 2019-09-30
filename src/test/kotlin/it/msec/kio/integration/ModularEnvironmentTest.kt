package it.msec.kio.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.URIO
import it.msec.kio.ask
import it.msec.kio.flatMap
import it.msec.kio.result.get
import it.msec.kio.runtime.unsafeRunSync
import org.junit.Test

interface Dep1 {
    fun doDep1(p: String): Int = p.length
}

interface Dep2 {
    fun doDep2(p: Int): String = "Hello $p"
}

object MyEnv : Dep1, Dep2

class ModularEnvironmentTest {

    @Test
    fun `environment can be composed by using multiple modules (interfaces)`() {

        val r = useDep1<MyEnv>("John")
                .flatMap { x -> useDep2<MyEnv>(x) }
                .unsafeRunSync(MyEnv)
                .get()

        assertThat(r).isEqualTo("Hello 4")
    }

    private fun <R : Dep1> useDep1(name: String): URIO<R, Int> =
            ask { env -> env.doDep1(name) }

    private fun <R : Dep2> useDep2(a: Int): URIO<R, String> =
            ask { env -> env.doDep2(a) }

}
