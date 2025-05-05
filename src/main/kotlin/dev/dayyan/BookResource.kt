package dev.dayyan

import dev.dayyan.persistence.Book
import dev.dayyan.persistence.BookRepository
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.UriBuilder

@Path("/hello")
class BookResource(private val bookRepository: BookRepository) {
    @POST
    @Path("/books")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addBook(book: Book): Response {
        val createdBook = bookRepository.create(book)
        val uri = UriBuilder.fromPath("/hello/books/{id}").build(createdBook.id)
        return Response.created(uri).entity(createdBook).build()
    }

    @GET
    @Path("/books")
    @Produces(MediaType.APPLICATION_JSON)
    fun getBooks(): Response {
        val books = bookRepository.getAll()
        return Response.ok(books).build()
    }

    @GET
    @Path("/books/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getBook(id: Int): Response {
        val book = bookRepository.getById(id)
        return if (book != null) {
            Response.ok(book).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}
