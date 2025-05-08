package dev.dayyan.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.quarkus.logging.Log
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

        private val allowedApiKeys: Set<String> by lazy {
            val keys: Set<String> =
                if (configuredApiKeysJson.isNotBlank()) {
                    try {
                        val apiKeyDetails: Map<String, String> = objectMapper.readValue(configuredApiKeysJson)
                        val loadedKeys = apiKeyDetails.keys
                        Log.info("Successfully loaded ${loadedKeys.size} API keys.")

                        apiKeyDetails.forEach { (key, description) ->
                            Log.debug("Loaded API Key (prefix): ${key.take(8)}... for Description: $description")
                        }
                        loadedKeys
                    } catch (e: IOException) {
                        Log.error("Failed to parse DEVELOPER_API_KEYS_JSON: ${e.message}", e)
                        emptySet()
                    }
                } else {
                    emptySet()
                }

            if (keys.isEmpty()) {
                val reason =
                    if (configuredApiKeysJson.isBlank()) {
                        "DEVELOPER_API_KEYS_JSON is blank"
                    } else {
                        "DEVELOPER_API_KEYS_JSON was not blank but resulted in zero keys " +
                            "(possibly due to parsing error or empty JSON object)"
                    }
                Log.warn(
                    "No API keys configured or loaded. Reason: $reason. " +
                        "Endpoints protected by @ApiKeyProtected may be inaccessible if any exist.",
                )
            }
            keys
        }

        private fun abortWithUnauthorized(
            requestContext: ContainerRequestContext,
            errorMessage: String,
        ) {
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"$errorMessage\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build(),
            )
        }

        override fun filter(requestContext: ContainerRequestContext) {
            val authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)

            val clientIp =
                requestContext.getHeaderString("X-Forwarded-For")
                    ?.split(",")?.firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
                    ?: requestContext.uriInfo.requestUri.host
                    ?: "unknown"

            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX, ignoreCase = true)) {
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: " +
                        "Authorization header missing or not Bearer type.",
                )
                val errorMessage = "Unauthorized - Authorization header missing or not Bearer type."
                abortWithUnauthorized(requestContext, errorMessage)
                return
            }

            val providedApiKey = authorizationHeader.substring(BEARER_PREFIX.length).trim()

            if (providedApiKey.isEmpty()) {
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: Bearer token is empty.",
                )
                val errorMessage = "Unauthorized - Bearer token is empty."
                abortWithUnauthorized(requestContext, errorMessage)
                return
            }

            if (!allowedApiKeys.contains(providedApiKey)) {
                Log.warn(
                    "Unauthorized access attempt from IP $clientIp to path [${requestContext.uriInfo.path}]: " +
                        "Invalid API Key (Bearer Token). Provided Key (prefix): ${providedApiKey.take(8)}...",
                )
                val errorMessage = "Unauthorized - Invalid API key."
                abortWithUnauthorized(requestContext, errorMessage)
                return
            }

            Log.info(
                "API access granted via Bearer token (prefix: ${providedApiKey.take(3)}...)" +
                    "from IP $clientIp to path [${requestContext.uriInfo.path}]",
            )
        }
    }
