package dev.dayyan.persistence

import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.jooq.generated.tables.Authors
import org.jooq.generated.tables.Books
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class BookRepositoryTest {
    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var dsl: DSLContext

    // Using high IDs to avoid conflicts with existing data
    private var testId = 100

    @BeforeEach
    @Transactional
    fun setup() {
        // Clean up test data from previous runs
        dsl.deleteFrom(Books.BOOKS)
            .where(Books.BOOKS.ID.ge(100))
            .execute()

        // Make sure we have a test author to reference
        val authorExists =
            dsl.fetchExists(
                dsl.selectFrom(Authors.AUTHORS)
                    .where(Authors.AUTHORS.ID.eq(1)),
            )

        if (!authorExists) {
            dsl.insertInto(Authors.AUTHORS)
                .set(Authors.AUTHORS.ID, 1)
                .set(Authors.AUTHORS.FIRST_NAME, "Test")
                .set(Authors.AUTHORS.LAST_NAME, "Author")
                .execute()
        }
    }

    @AfterEach
    @Transactional
    fun tearDown() {
        // Clean up our test data
        dsl.deleteFrom(Books.BOOKS)
            .where(Books.BOOKS.ID.ge(100))
            .execute()
    }

    @Test
    @Transactional
    fun testCreateBook() {
        // Given
        val id = testId++
        val book =
            Book(
                id = id,
                title = "Test Book",
                description = "Test Description",
                publishedYear = 2023,
                authorId = 1,
            )

        // When
        val created = bookRepository.create(book)

        // Then
        assertThat(created.id).isEqualTo(id)
        assertThat(created.title).isEqualTo("Test Book")
        assertThat(created.description).isEqualTo("Test Description")
        assertThat(created.publishedYear).isEqualTo(2023)
        assertThat(created.authorId).isEqualTo(1)

        // Verify in database
        val fromDb =
            dsl.selectFrom(Books.BOOKS)
                .where(Books.BOOKS.ID.eq(id))
                .fetchOne()

        assertThat(fromDb).isNotNull
        assertThat(fromDb?.id).isEqualTo(id)
    }

    @Test
    @Transactional
    fun testFindById() {
        // Given - insert directly with DSL
        val id = testId++
        dsl.insertInto(Books.BOOKS)
            .set(Books.BOOKS.ID, id)
            .set(Books.BOOKS.TITLE, "Test Book Find")
            .set(Books.BOOKS.DESCRIPTION, "Test Description for Find")
            .set(Books.BOOKS.PUBLISHED_YEAR, 2023)
            .set(Books.BOOKS.AUTHOR_ID, 1)
            .execute()

        // When
        val found = bookRepository.findById(id)

        // Then
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(id)
        assertThat(found?.title).isEqualTo("Test Book Find")
        assertThat(found?.description).isEqualTo("Test Description for Find")
        assertThat(found?.publishedYear).isEqualTo(2023)
        assertThat(found?.authorId).isEqualTo(1)
    }

    @Test
    fun testFindByIdNotFound() {
        // When
        val found = bookRepository.findById(999)

        // Then
        assertThat(found).isNull()
    }
}
