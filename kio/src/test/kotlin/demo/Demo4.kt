package demo.Demo4

import it.msec.kio.*
import it.msec.kio.runtime.RuntimeSuspended.unsafeRunSync

inline class EmailAddress(val value: String)
inline class CustomerId(val value: String)
data class Customer(val id: CustomerId, val name: String, val surname: String, val email: EmailAddress)
data class Email(val body: String, val dest: EmailAddress)

sealed class Error
data class CustomerNotFound(val id: CustomerId) : Error()
data class BannedAddressError(val email: Email) : Error()

// ----------------------------

interface EmailServer {
    fun sendEmail(email: Email): IO<BannedAddressError, Unit>
}

interface CustomerRepository {
    fun search(id: CustomerId): Option<Customer>
}

interface Log {
    fun logMessage(s: String): UIO<Unit>
}

// ----------------------------

fun <R> run(customerId: CustomerId): KIO<R, Nothing, Unit>
        where R : EmailServer,
              R : CustomerRepository,
              R : Log = withR {
    search(customerId)
            .mapError { CustomerNotFound(customerId) }
            .flatMap { customer -> sendWelcomeEmail(customer) }
            .tryRecover { error -> handleError(error) }
}

fun <R : Log> handleError(e: Error): KIO<R, Nothing, Unit> =
        withR { logMessage("Error: $e") }

fun <R> sendWelcomeEmail(customer: Customer): KIO<R, BannedAddressError, Unit>
        where R : EmailServer,
              R : Log =
        withR {
            val email: Email = composeEmail(customer)
            sendEmail(email)

        }

fun composeEmail(c: Customer): Email =
        Email("Welcome ${c.name}!", c.email)













// ----------------------------

object Env :
        EmailServer by DemoEmailServer,
        CustomerRepository by DemoCustomerRepository,
        Log by DemoLog

fun main() {
    val program: KIO<Env, Nothing, Unit> = run(CustomerId("33"))

    unsafeRunSync(program, Env)
}










// ----------------------------

object DemoEmailServer : EmailServer {
    override fun sendEmail(email: Email): IO<BannedAddressError, Unit> = effect {
        println("DemoEmailServer: sending e-mail $email")
    }
}

object DemoCustomerRepository : CustomerRepository {
    override fun search(id: CustomerId): Option<Customer> =
            if (id == CustomerId("33"))
                just(Customer(CustomerId("33"), "El", "Profesor", EmailAddress("el@profesor.es")))
            else
                empty()
}

object DemoLog : Log {
    override fun logMessage(s: String): UIO<Unit> = effect {
        println("Log: $s")
    }
}
