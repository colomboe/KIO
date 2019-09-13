package it.msec.kio.kio

import it.msec.kio.EnvTask
import it.msec.kio.askEnv
import it.msec.kio.flatMap
import it.msec.kio.result.get
import it.msec.kio.runtime.unsafeRunSync
import org.junit.Test

interface Dep1 {
    fun doDep1(p: String): Int
}

interface Dep2 {
    fun doDep2(p: Int): String
}

object MyEnv : Dep1, Dep2 {

    override fun doDep1(p: String): Int {
        return 33;
    }

    override fun doDep2(p: Int): String {
        return "pippo $p"
    }

}

class EnvTest {

    @Test
    fun envTest() {

        val x: EnvTask<MyEnv, String> =
                useDep1("bau")
                        .flatMap { x -> useDep2(x) }

        val z = x.unsafeRunSync(MyEnv).get()

        println(z)

    }

    fun useDep1(name: String): EnvTask<Dep1, Int> =
            askEnv { env -> env.doDep1(name) }

    fun useDep2(a: Int): EnvTask<Dep2, String> =
            askEnv { env -> env.doDep2(a) }

}
