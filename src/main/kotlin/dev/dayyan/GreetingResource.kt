package dev.dayyan

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/hello")
class GreetingResource (private val service: GreetingService) {

    @GET
    @Path("/greeting/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    fun greeting(name: String): String = service.greeting(name)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(): String = "Hello from Quarkus REST"
}