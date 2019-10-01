package it.msec.kio

import assertk.assertThat
import assertk.assertions.isEqualTo
import it.msec.kio.result.getOrNull
import it.msec.kio.runtime.unsafeRunSync

data class Account(val username: String)

fun unsafeRetrieveAccountFromDB(id: String): Account =
        throw IllegalArgumentException("No account found with id = $id")

fun safeRetrieveAccountFromDB(userId: String): Task<Account> =
        unsafe { unsafeRetrieveAccountFromDB(userId) }

fun printToConsole(s: String): Task<Unit> =
        unsafe { println(s) }

fun readFromConsole(s: String): Task<String?> =
        printToConsole(s).flatMap { unsafe { "text from console" } }

fun retrieveUsername(userId: String): UIO<String> =
        safeRetrieveAccountFromDB(userId)
                .map { it.username }
                .peekError { e -> printToConsole("Warning: ${e.message}") }
                .recover { "anonymous" }

fun interactOnConsole(username: String): Task<String> =
        printToConsole("Welcome $username")
                .flatMap { readFromConsole("Enter some text:") }
                .map { it.orEmpty() }

fun main() {

    val userInput = retrieveUsername("123")
            .flatMap(::interactOnConsole)
            .toOption()
            .unsafeRunSync()
            .getOrNull()

    assertThat(userInput).isEqualTo("text from console")
}
