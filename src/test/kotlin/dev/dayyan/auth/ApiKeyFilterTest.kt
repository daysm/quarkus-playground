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
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val requestContext: ContainerRequestContext = mockk(relaxed = true)
    private val uriInfo: UriInfo = mockk()
    private val validApiKey = "test-key-123"
    private val configuredApiKeysJson = """{"$validApiKey":"Test API Key Description"}"""
    private val apiKeyFilter = ApiKeyFilter(objectMapper, configuredApiKeysJson)

    @BeforeEach
    fun setUp() {
        apiKeyFilter.init()

        every { requestContext.uriInfo } returns uriInfo
        every { uriInfo.path } returns "/api/protected/resource"
        every { uriInfo.requestUri } returns URI.create("http://localhost/api/protected/resource")
        every { requestContext.getHeaderString("X-Forwarded-For") } returns null
    }

    @Test
    fun `it allows access when a valid API key is provided`() {
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns "Bearer $validApiKey"
        apiKeyFilter.filter(requestContext)
        verify(exactly = 0) { requestContext.abortWith(any()) }
    }

    @Test
    fun `it blocks access when no valid API key is provided`() {
        val invalidApiKey = "invalid-key"
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns "Bearer $invalidApiKey"
        apiKeyFilter.filter(requestContext)
        verify { requestContext.abortWith(any()) }
    }

    @Test
    fun `it blocks access when no authorization header is set`() {
        every { requestContext.getHeaderString(HttpHeaders.AUTHORIZATION) } returns null
        apiKeyFilter.filter(requestContext)
        verify { requestContext.abortWith(any()) }
    }
}
