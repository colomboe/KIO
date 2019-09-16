package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import it.msec.kio.common.functions.identity
import it.msec.kio.result.Failure
import it.msec.kio.result.Result
import it.msec.kio.result.get
import it.msec.kio.result.getOrThrow
import it.msec.kio.runtime.unsafeRunSync
import it.msec.kio.runtime.unsafeRunSyncAndGet
import org.junit.Test

class KIOFunctionsTest {

    @Test
    fun `task factory method`() {
        val t: Task<String> = task { "Hello" }
        val s: String = t.unsafeRunSync().get()
        assertThat(s).isEqualTo("Hello")
    }


    @Test
    fun `taskR factory method`() {
        val t: TaskR<Int, String> = taskR { "Hello" }
        val s: String = t.unsafeRunSync(33).get()
        assertThat(s).isEqualTo("Hello")
    }

    @Test
    fun `just factory method`() {
        val t: Task<String> = just("Hello")
        val s: String = t.unsafeRunSync().get()
        assertThat(s).isEqualTo("Hello")
    }

    @Test
    fun `justR factory method`() {
        val t: TaskR<Int, String> = justR("Hello")
        val s: String = t.unsafeRunSync(33).get()
        assertThat(s).isEqualTo("Hello")
    }


    @Test
    fun `failure factory method`() {
        val t: BIO<String, Int> = failure("Hello")
        val r: Result<String, Int> = t.unsafeRunSync()
        assertThat(r).isInstanceOf(Failure::class).transform { it.error}.isEqualTo("Hello")
    }

    @Test
    fun `failureR factory method`() {
        val t: KIO<Int, String, Int> = failureR("Hello")
        val r: Result<String, Int> = t.unsafeRunSync(33)
        assertThat(r).isInstanceOf(Failure::class).transform { it.error}.isEqualTo("Hello")
    }

    @Test
    fun `unsafe factory method with success`() {
        val t: Try<String>  = unsafe { "Hello" }
        val r: String = t.unsafeRunSync().getOrThrow()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `unsafe factory method with failure`() {
        val t: Try<String>  = unsafe { throw RuntimeException("Hello") }
        val r: Result<Throwable, String> = t.unsafeRunSync()
        assertThat { r.getOrThrow() }.isFailure().isInstanceOf(RuntimeException::class)
    }

    @Test
    fun `unsafeR factory method with success`() {
        val t: KIO<Int, Throwable, String> = unsafeR { "Hello" }
        val r: String = t.unsafeRunSync(33).getOrThrow()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `unsafeR factory method with failure`() {
        val t: KIO<Int, Throwable, String>  = unsafeR { throw RuntimeException("Hello") }
        val r: Result<Throwable, String> = t.unsafeRunSync(33)
        assertThat { r.getOrThrow() }.isFailure().isInstanceOf(RuntimeException::class)
    }

    @Test
    fun `map transformation`() {
        val r: Int = just("Hello").map { it.length }.unsafeRunSync().get()
        assertThat(r).isEqualTo(5)
    }

    @Test
    fun `flatMap transformation with success`() {
        val r: Int = just("Hello").flatMap { just(it.length) }.unsafeRunSync().get()
        assertThat(r).isEqualTo(5)
    }

    @Test
    fun `flatMap transformation with failure`() {
        val r: Result<Int, Nothing> = just("Hello").flatMap { failure(it.length) }.unsafeRunSync()
        assertThat(r).isInstanceOf(Failure::class).transform { it.error}.isEqualTo(5)
    }

    @Test
    fun `askR provide the injected environment`() {
        val r: String = askR { env: Int -> env.toString() }.unsafeRunSync(33).get()
        assertThat(r).isEqualTo("33")
    }

    @Test
    fun `mapError transformation`() {
        val r: Result<Int, Nothing> = failure("Hello").mapError { it.length }.unsafeRunSync()
        assertThat(r).isInstanceOf(Failure::class).transform { it.error}.isEqualTo(5)
    }

    @Test
    fun `swap changes failure with success`() {
        val r: String = failure("Hello").swap().unsafeRunSync().get()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `attempt converts Task to BIO`() {
        val r: Result<Throwable, String> = task { throw RuntimeException("Hello") }.attempt().unsafeRunSync()
        assertThat { r.getOrThrow() }.isFailure().isInstanceOf(RuntimeException::class)
    }

    @Test
    fun `recover error with a new value`() {
        val r: String = failure(33).recover { "Hello" }.unsafeRunSync().get()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `try to recover error with a new effect with success`() {
        val r: String = failure(RuntimeException("33")).tryRecover { just("Hello") }
                .unsafeRunSync().getOrThrow()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `try to recover error with a new effect with failure`() {
        val r: Result<RuntimeException, Nothing> = failure(RuntimeException("Hello")).tryRecover { failure(IllegalArgumentException("33")) }.unsafeRunSync()
        assertThat { r.getOrThrow() }.isFailure().isInstanceOf(IllegalArgumentException::class)
    }

    @Test
    fun `fold provide a task with success`() {
        val e: BIO<Int, String> = just("Hello")
        val r: String = e.fold({ "Not now" }, { it }).unsafeRunSyncAndGet()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `fold provide a task with failure`() {
        val e: BIO<String, Int> = failure("Hello")
        val r: String = e.fold({ it }, { "Not now" }).unsafeRunSyncAndGet()
        assertThat(r).isEqualTo("Hello")
    }

    @Test
    fun `bimap maps both success and error`() {
        val r1: Int = just("Hello").bimap(::identity, { it.length }).unsafeRunSyncAndGet()
        val r2: Int = failure("World!!!").bimap({ it.length }, ::identity).swap().unsafeRunSyncAndGet()
        assertThat(r1).isEqualTo(5)
        assertThat(r2).isEqualTo(8)
    }

    @Test
    fun `filterTo maps value to error when predicate is false`() {
        val r: Result<Int, String> = just("Hello").filterTo({ it.length }, { it == "Hello!!!" }).unsafeRunSync()
        assertThat(r).isInstanceOf(Failure::class).transform { it.error }.isEqualTo(5)
    }
}
