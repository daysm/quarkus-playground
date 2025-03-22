package dev.dayyan

import com.fasterxml.jackson.databind.ObjectMapper
import dev.dayyan.persistence.Book
import dev.dayyan.persistence.BookRepository
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@QuarkusTest
class BookResourceTest {
    @Inject
    lateinit var bookRepository: BookRepository

    private val client = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    @Test
    fun testGetBookFound() {
        val book = Book(title = "Test title")
        val persistedBook = bookRepository.create(book)

        val existingBookId = persistedBook.id

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/hello/books/$existingBookId"))
                .header("Accept", MediaType.APPLICATION_JSON)
                .GET()
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("Content-Type").get())
            .contains(MediaType.APPLICATION_JSON)
    }

    @Test
    fun testCreateBook() {
        val book = Book(title = "New Book")
        val bookJson = objectMapper.writeValueAsString(book)

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/hello/books"))
                .header("Accept", MediaType.APPLICATION_JSON)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(bookJson))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(201)
        val location = response.headers().firstValue("Location").orElse("")
        assertThat(location).isNotEmpty.contains("/hello/books/")
    }

    @Test
    fun testGetBookNotFound() {
        val nonExistingBookId = 99999 // Ensure this ID does not exist in your test database

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/hello/books/$nonExistingBookId"))
                .header("Accept", MediaType.APPLICATION_JSON)
                .GET()
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(404)
    }
}
