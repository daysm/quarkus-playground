package dev.dayyan.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.logging.Log
import jakarta.annotation.PostConstruct
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.io.IOException
import java.util.Optional

@Provider
@ApiKeyProtected
@ApplicationScoped
class ApiKeyFilter
    @Inject
    constructor(
        private val objectMapper: ObjectMapper,
        @ConfigProperty(name = "DEVELOPER_API_KEYS_JSON")
        private val configuredApiKeysJson: String,
    ) : ContainerRequestFilter {
        companion object {
            private const val BEARER_PREFIX = "Bearer "
        }

        private var allowedApiKeys = emptySet<String>()

        @PostConstruct
        fun init() {
            if (configuredApiKeysJson.isNotBlank()) {
                try {
                    val apiKeyDetails: Map<String, String> = objectMapper.readValue(configuredApiKeysJson)
                    allowedApiKeys = apiKeyDetails.keys
                    Log.info("Successfully loaded ${allowedApiKeys.size} API keys.")

                    apiKeyDetails.forEach { (key, description) ->
                        Log.debug("Loaded API Key (prefix): ${key.take(8)}... for Description: $description")
                    }
                } catch (e: IOException) {
                    Log.error("Failed to parse DEVELOPER_API_KEYS_JSON: ${e.message}", e)
                }
            }

            if (allowedApiKeys.isEmpty()) {
                Log.warn("No API keys configured or loaded. Endpoints protected by @ApiKeyProtected may be inaccessible if any exist.")
            }
        }

        override fun filter(requestContext: ContainerRequestContext) {
            val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)

            val clientIp =
                Optional.ofNullable(requestContext.getHeaderString("X-Forwarded-For"))
                    .map { ips -> ips.split(",").first().trim() }
                    .orElseGet { requestContext.uriInfo.requestUri.host ?: "unknown" }

            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: " +
                        "Authorization header missing or not Bearer type.",
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
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: Bearer token is empty.",
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
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: " +
                        "Invalid API Key (Bearer Token). Provided Key (prefix): ${providedApiKey.take(
                            8,
                        )}...",
                )
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Unauthorized - Invalid API key.\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build(),
                )
                return
            }

            Log.info(
                "API access granted via Bearer token (prefix: ${providedApiKey.take(
                    3,
                )}...) from IP $clientIp to path [${requestContext.uriInfo.path}]",
            )
        }
    }
