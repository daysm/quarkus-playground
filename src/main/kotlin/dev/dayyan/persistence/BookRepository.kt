package dev.dayyan.persistence

import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.jooq.DSLContext
import org.jooq.generated.tables.Book.BOOK
import org.jooq.generated.tables.records.BookRecord

@ApplicationScoped
class BookRepository(
    private val dsl: DSLContext,
) {
    fun findById(id: Int): Book? {
        return dsl
            .selectFrom(BOOK)
            .where(BOOK.ID.eq(id))
            .fetchOne()
            ?.toDomain()
    }

    @Transactional
    fun create(book: Book): Book {
        require(book.id == null) { "id will be set by database" }
        val record = book.fromDomain()
        record.store()
        return record.toDomain()
    }

    private fun BookRecord.toDomain(): Book {
        return Book(
            id = this.id,
            title = this.title,
            description = this.description,
            publishedYear = this.publishedYear,
            authorId = this.authorId,
        )
    }

    private fun Book.fromDomain(): BookRecord {
        val record = dsl.newRecord(BOOK)
        id?.let { record.id = it }
        record.title = this.title
        record.description = this.description
        record.publishedYear = this.publishedYear
        record.authorId = this.authorId
        return record
    }
}

data class Book(
    val id: Int? = null,
    val title: String,
    val description: String? = null,
    val publishedYear: Int? = null,
    val authorId: Int? = null,
)
