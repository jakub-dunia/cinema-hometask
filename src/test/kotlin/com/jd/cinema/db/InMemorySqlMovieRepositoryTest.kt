package com.jd.cinema.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.*

class InMemorySqlMovieRepositoryTest {

    private lateinit var database: Database
    private lateinit var repository: InMemorySqlMovieRepository

    @BeforeTest
    fun setup() {
        database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        repository = InMemorySqlMovieRepository(database)
    }

    @AfterTest
    fun tearDown() {
        transaction(database) {
            InMemorySqlMovieRepository.DbMovies.dropStatement().forEach { exec(it) }
        }
    }

    @Test
    fun shouldReturnAllMovies() {
        // When
        val movies = repository.fetchAllMovies()

        // Then
        assertEquals(9, movies.size)
        assertTrue(movies.any { it.title == "The Fast and the Furious" })
        assertTrue(movies.any { it.externalId == "imdb:tt0322259" })
    }

    @Test
    fun shouldReturnSingleMovie() {
        // Given
        val allMovies = repository.fetchAllMovies()
        val validMovie = allMovies.first()

        // When
        val fetchedMovie = repository.fetchMovie(validMovie.id)

        // Then
        assertNotNull(fetchedMovie)
        assertEquals(validMovie.id, fetchedMovie.id)
        assertEquals(validMovie.title, fetchedMovie.title)
        assertEquals(validMovie.externalId, fetchedMovie.externalId)
    }

    @Test
    fun shouldReturnNullOnInvalidId() {
        // Given
        val invalidId = UUID.randomUUID()

        // When
        val fetchedMovie = repository.fetchMovie(invalidId)

        // Then
        assertNull(fetchedMovie)
    }
}

