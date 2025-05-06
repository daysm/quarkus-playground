package dev.dayyan.auth

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.HttpHeaders // Standard JAX-RS HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.io.IOException
import java.util.Collections
import java.util.Optional

@Provider
@ApiKeyProtected
@ApplicationScoped
class ApiKeyFilter : ContainerRequestFilter {
    companion object {
        private val logger: Logger = Logger.getLogger(ApiKeyFilter::class.java)
        private const val BEARER_PREFIX = "Bearer "
    }

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    @ConfigProperty(name = "DEVELOPER_API_KEYS_JSON", defaultValue = "{}")
    lateinit var configuredApiKeysJson: String

    private var allowedApiKeys: Set<String> = Collections.emptySet()

    @PostConstruct
    fun init() {
        if (::configuredApiKeysJson.isInitialized && configuredApiKeysJson.isNotBlank() && configuredApiKeysJson != "{}") {
            try {
                val typeRef = object : TypeReference<Map<String, String>>() {}
                val apiKeyDetails: Map<String, String> = objectMapper.readValue(configuredApiKeysJson, typeRef)
                allowedApiKeys = apiKeyDetails.keys
                logger.infof("Successfully loaded %d API keys.", allowedApiKeys.size)

                apiKeyDetails.forEach { (key, description) ->
                    logger.debugf("Loaded API Key (prefix): %s... for Description: %s", key.take(8), description)
                }
            } catch (e: IOException) {
                logger.errorf(e, "Failed to parse DEVELOPER_API_KEYS_JSON: %s", e.message)
            }
        }

        if (allowedApiKeys.isEmpty()) {
            logger.warn("No API keys configured or loaded. Endpoints protected by @ApiKeyProtected may be inaccessible if any exist.")
        }
    }

    override fun filter(requestContext: ContainerRequestContext) {
        val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)

        val clientIp =
            Optional.ofNullable(requestContext.getHeaderString("X-Forwarded-For"))
                .map { ips -> ips.split(",").first().trim() }
                .orElseGet { requestContext.uriInfo.requestUri.host ?: "unknown" }

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            logger.warnf(
                "Unauthorized access attempt from IP [%s] to path [%s]: Authorization header missing or not Bearer type.",
                clientIp,
                requestContext.uriInfo.path,
            )
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Unauthorized - Authorization header missing or not Bearer type.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build(),
            )
            return
        }

        val providedApiKey = authorizationHeader.substring(BEARER_PREFIX.length).trim()

        if (providedApiKey.isEmpty()) {
            logger.warnf(
                "Unauthorized access attempt from IP [%s] to path [%s]: Bearer token is empty.",
                clientIp,
                requestContext.uriInfo.path,
            )
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Unauthorized - Bearer token is empty.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build(),
            )
            return
        }

        if (!allowedApiKeys.contains(providedApiKey)) {
            logger.warnf(
                "Unauthorized access attempt from IP [%s] to path [%s]: Invalid API Key (Bearer Token). Provided Key (prefix): %s...",
                clientIp,
                requestContext.uriInfo.path,
                providedApiKey.take(8),
            )
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Unauthorized - Invalid API Key.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build(),
            )
            return
        }

        logger.infof(
            "API access granted via Bearer token (prefix: %s...) from IP [%s] to path [%s]",
            providedApiKey.take(8),
            clientIp,
            requestContext.uriInfo.path,
        )
    }
}
