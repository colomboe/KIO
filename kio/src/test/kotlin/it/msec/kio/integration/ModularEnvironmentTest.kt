package it.msec.kio.integration

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.*
import it.msec.kio.result.get
import it.msec.kio.runtime.Runtime
import org.junit.Test

interface Dep1 {
    fun doDep1(p: String): Int = p.length
}

interface Dep2 {
    fun doDep2(p: Int): UIO<String> = effect { "Hello $p" }
}

interface Both : Dep1, Dep2

object MyEnv : Both

class ModularEnvironmentTest {

    @Test
    fun `environment can be composed by using multiple modules (interfaces)`() {

        val program  = programDependingFromBoth()
        val r = Runtime.unsafeRunSync(program, MyEnv).get()
        assertThat(r).isEqualTo("Hello 4")
    }

    private fun programDependingFromBoth(): URIO<Both, String> =
            callDep1("John").flatMap(this::callDep2)

    private fun callDep1(p: String): URIO<Dep1, Int> = askPure { env -> env.doDep1(p) }
    private fun callDep2(p: Int): URIO<Dep2, String> = ask { env -> env.doDep2(p) }

}
