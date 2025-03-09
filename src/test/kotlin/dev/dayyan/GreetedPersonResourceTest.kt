package dev.dayyan

import io.quarkus.test.junit.QuarkusTest
import jakarta.ws.rs.core.MediaType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@QuarkusTest
class GreetedPersonResourceTest {

    private val client = HttpClient.newHttpClient()

    @Test
    fun testGreetingEndpoint() {
        val name = "test"
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/hello/greeting/$name"))
            .header("Accept", MediaType.TEXT_PLAIN)
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).isEqualTo("hello $name")
    }

    @Test
    fun testHelloEndpoint() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/hello")) // Adjust port if needed
            .header("Accept", MediaType.TEXT_PLAIN)
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).isEqualTo("Hello from Quarkus REST")
    }

}