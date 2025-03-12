package dev.dayyan.persistence

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.jooq.DSLContext
import org.jooq.generated.tables.Books
import org.jooq.generated.tables.records.BooksRecord

@ApplicationScoped
class BookRepository(
    private val dsl: DSLContext,
) {
    fun findById(id: Int): Book? {
        return dsl
            .selectFrom(Books.BOOKS)
            .where(Books.BOOKS.ID.eq(id))
            .fetchOne()
            ?.toBook()
    }

    @Transactional
    fun create(book: Book): Book {
        val record = dsl.newRecord(Books.BOOKS)
        // Set ID if provided, otherwise generate a new ID
        if (book.id != null) {
            record.id = book.id
        } else {
            // For a real application, you would use a sequence or other reliable ID generation mechanism
            val maxId =
                dsl.select(org.jooq.impl.DSL.max(Books.BOOKS.ID))
                    .from(Books.BOOKS)
                    .fetchOneInto(Int::class.java) ?: 0
            record.id = maxId + 1
        }

        record.title = book.title
        record.description = book.description
        record.publishedYear = book.publishedYear
        record.authorId = book.authorId

        record.store()

        return record.toBook()
    }

    // Extension function to convert BooksRecord to Book data class
    private fun BooksRecord.toBook(): Book {
        return Book(
            id = this.id,
            title = this.title,
            description = this.description,
            publishedYear = this.publishedYear,
            authorId = this.authorId,
        )
    }
}

// Data class representing a Book entity
data class Book(
    val id: Int? = null,
    val title: String,
    val description: String? = null,
    val publishedYear: Int? = null,
    val authorId: Int? = null,
)
