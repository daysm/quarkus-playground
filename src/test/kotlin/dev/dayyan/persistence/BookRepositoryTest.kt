package dev.dayyan.persistence

import dev.dayyan.PostgresTestBase
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@QuarkusTest
class BookRepositoryTest : PostgresTestBase() {
    @Inject
    lateinit var sut: BookRepository

    @Test
    fun `it returns null when no book with id exists`() {
        val fromDb = sut.getById(1)
        assertThat(fromDb).isNull()
    }

    @Test
    fun `it returns book`() {
        val bookToCreate = Book(title = "title")
        val persistedBook = sut.create(bookToCreate)
        val retrievedBook = sut.getById(persistedBook.id!!)
        assertThat(retrievedBook).isEqualTo(persistedBook)
    }

    @Test
    fun `it handles clean up`() {
        val bookToCreate = Book(title = "title")
        val persistedBook = sut.create(bookToCreate)
        val retrievedBook = sut.getById(persistedBook.id!!)
        assertThat(retrievedBook).isEqualTo(persistedBook)
    }
}
