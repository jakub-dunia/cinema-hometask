package com.jd.cinema.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.*

class InMemorySqlScreeningRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: InMemorySqlScreeningRepository

    @BeforeTest
    fun setup() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        repository = InMemorySqlScreeningRepository(database)
    }

    @AfterTest
    fun tearDown() {
        transaction(database) {
            InMemorySqlScreeningRepository.DbScreening.dropStatement().forEach { exec(it) }
        }
    }

    @Test
    fun shouldAddNewScreening() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp = LocalDateTime.now()
        val price = 10

        // When
        val screening = repository.addScreening(movieId, timestamp, price)

        // Then
        assertEquals(movieId, screening.movieId)
        assertEquals(timestamp, screening.timestamp)
        assertEquals(price, screening.price)
    }

    @Test
    fun shouldReturnExistingScreeningWhenAddingNewWithSameTime() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp: LocalDateTime = LocalDateTime.parse("2022-10-01 12:00", LocalDateTimeSerializer.formatter)
        val price = 10
        val first = repository.addScreening(movieId, timestamp, price)

        // When
        val screening = repository.addScreening(movieId, timestamp, 20)

        // Then
        assertEquals(first.id, screening.id)
        assertEquals(movieId, screening.movieId)
        assertEquals(timestamp, screening.timestamp)
        assertEquals(first.price, screening.price)
    }

    @Test
    fun fetchScreeningsByMovieIdShouldReturnAllScreeningsForGivenMovie() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp1 = LocalDateTime.now()
        val timestamp2 = timestamp1.plusHours(2)
        repository.addScreening(movieId, timestamp1, 15)
        repository.addScreening(movieId, timestamp2, 20)

        // When
        val screenings = repository.fetchScreeningsByMovieId(movieId)

        // Then
        assertEquals(2, screenings.size)
        assertTrue(screenings.any { it.price == 15 })
        assertTrue(screenings.any { it.price == 20 })
    }

    @Test
    fun fetchScreeningsByMovieIdShouldReturnEmptySetIfNoScreeningsExist() {
        // Given
        val movieId = UUID.randomUUID()

        // When
        val screenings = repository.fetchScreeningsByMovieId(movieId)

        // Then
        assertTrue(screenings.isEmpty())
    }

    @Test
    fun shouldDeleteScreening() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp = LocalDateTime.now()
        repository.addScreening(movieId, timestamp, 10)

        // When
        val result = repository.deleteScreening(movieId, timestamp)

        // Then
        assertTrue(result)
        assertTrue(repository.fetchScreeningsByMovieId(movieId).isEmpty())
    }

    @Test
    fun deleteScreeningShouldReturnFalseIfNoScreeningExists() {
        // Given
        val movieId = UUID.randomUUID()
        val timestamp = LocalDateTime.now()

        // When
        val result = repository.deleteScreening(movieId, timestamp)

        // Then
        assertFalse(result)
    }
}
