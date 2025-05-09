package dev.dayyan.auth

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.UriInfo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI

class ApiKeyFilterTest {
    private val requestContext: ContainerRequestContext = mockk(relaxed = true)
    private val uriInfo: UriInfo = mockk()
    private val validApiKey = "test-key-123"
    private val path = "example/path"
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val configuredApiKeysJson = """{"$validApiKey":"Test API Key Description"}"""
    private val sut = ApiKeyFilter(objectMapper, configuredApiKeysJson)

    @BeforeEach
    fun setUp() {
        every { requestContext.uriInfo } returns uriInfo
        every { uriInfo.path } returns path
        every { uriInfo.requestUri } returns URI.create("http://localhost/$path")
        every { requestContext.getHeaderString("X-Forwarded-For") } returns null
    }

    @Test
    fun `it does not abort when a valid API key is provided`() {
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns "Bearer $validApiKey"
        sut.filter(requestContext)
        verify(exactly = 0) { requestContext.abortWith(any()) }
    }

    @Test
    fun `it aborts when no valid API key is provided`() {
        val invalidApiKey = "invalid-key"
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns "Bearer $invalidApiKey"
        sut.filter(requestContext)
        verify { requestContext.abortWith(any()) }
    }

    @Test
    fun `it aborts when no authorization header is set`() {
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns null
        sut.filter(requestContext)
        verify { requestContext.abortWith(any()) }
    }
}
