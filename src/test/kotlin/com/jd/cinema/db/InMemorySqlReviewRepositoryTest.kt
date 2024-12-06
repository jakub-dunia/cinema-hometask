package com.jd.cinema.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.*

class InMemorySqlReviewRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: InMemorySqlReviewRepository

    @BeforeTest
    fun setup() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        repository = InMemorySqlReviewRepository(database)
    }

    @AfterTest
    fun tearDown() {
        transaction(database) {
            InMemorySqlReviewRepository.DbReview.dropStatement().forEach { exec(it) }
        }
    }

    @Test
    fun shouldAddReview() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp = LocalDateTime.now()
        val rating = 5

        // When
        val review = repository.addReview(movieId, rating, timestamp)

        // Then
        assertNotNull(review)
        assertEquals(movieId, review.movieId)
        assertEquals(rating, review.rating)
        assertEquals(timestamp, review.timestamp)
    }

    @Test
    fun shouldFetchAllReviews() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp1 = LocalDateTime.now()
        val timestamp2 = timestamp1.plusHours(1)
        repository.addReview(movieId, 4, timestamp1)
        repository.addReview(movieId, 5, timestamp2)

        // When
        val reviews = repository.fetchAllReviews(movieId)

        // Then
        assertEquals(2, reviews.size)
        assertTrue(reviews.any { it.rating == 4 })
        assertTrue(reviews.any { it.rating == 5 })
    }

    @Test
    fun shouldReturnEmptyListOnMovieWithNoReviews() {
        // Given
        val movieId = UUID.randomUUID()

        // When
        val reviews = repository.fetchAllReviews(movieId)

        // Then
        assertTrue(reviews.isEmpty())
    }

    @Test
    fun shouldReturnCorrectAverageRating() {
        // Given
        val movieId = UUID.randomUUID()
        repository.addReview(movieId, 4, LocalDateTime.now())
        repository.addReview(movieId, 5, LocalDateTime.now().plusHours(1))

        // When
        val averageRating = repository.getAverageRating(movieId)

        // Then
        assertEquals(4.5, averageRating, 0.01)
    }

    @Test
    fun sohuldReturnNaNRatingForMovieWithNoReviews() {
        // Given
        val movieId = UUID.randomUUID()

        // When
        val averageRating = repository.getAverageRating(movieId)

        // Then
        assertTrue(averageRating.isNaN())
    }
}
