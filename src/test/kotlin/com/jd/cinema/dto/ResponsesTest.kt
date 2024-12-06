package com.jd.cinema.dto

import com.jd.cinema.db.Movie
import com.jd.cinema.db.Screening
import com.jd.cinema.integrations.OmdbResponse
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResponsesTests {

    @Test
    fun transformOmdbResponseToMovieDetailsResponseFromValidOmdbResponse() {
        // given
        val movieId = UUID.randomUUID()
        val omdbResponse = OmdbResponse("Inception", "2010", "PG-13")

        // when
        val result = transformOmdbResponseToMovieDetailsResponse(omdbResponse)

        // then
        assertEquals("Inception", result.title)
        assertEquals("2010", result.year)
        assertEquals("PG-13", result.rated)
    }

    @Test
    fun transformOmdbResponseToMovieDetailsResponseFromNullOmdbResponse() {
        // given
        val movieId = UUID.randomUUID()

        // when
        val result = transformOmdbResponseToMovieDetailsResponse(null)

        // then
        assertNull(result.title)
        assertNull(result.year)
        assertNull(result.rated)
    }

    @Test
    fun transformMovieToMovieResponseFromValidData() {
        // given
        val movie = Movie(UUID.randomUUID(), "Inception", "imdb:tt1375666")
        val screenings = setOf(
            Screening(UUID.randomUUID(), movie.id, LocalDateTime.parse("2024-12-01T20:00:00"), 12_00),
            Screening(UUID.randomUUID(), movie.id, LocalDateTime.parse("2024-12-02T18:00:00"), 10_00)
        )
        val omdbResponse = OmdbResponse("Inception", "2010", "PG-13")

        // when
        val result = transformMovieToMovieResponse(movie, screenings, omdbResponse)

        // then
        assertEquals(movie.id, result.id)
        assertEquals(2, result.screenings.size)
        assertEquals("Inception", result.details?.title)
        assertEquals("2010", result.details?.year)
        assertEquals("PG-13", result.details?.rated)
    }

    @Test
    fun transformMovieToMovieResponseWithNoScreeningsAndNullOmdbResponse() {
        // given
        val movie = Movie(UUID.randomUUID(), "Interstellar", "imdb:tt0816692")
        val screenings = emptySet<Screening>()

        // when
        val result = transformMovieToMovieResponse(movie, screenings, null)

        // then
        assertEquals(movie.id, result.id)
        assertEquals(0, result.screenings.size)
        assertNull(result.details?.title)
        assertNull(result.details?.year)
        assertNull(result.details?.rated)
    }

    @Test
    fun transformScreeningToScreeningResponseFromValidScreening() {
        // given
        val screeningId = UUID.randomUUID()
        val movieId = UUID.randomUUID()
        val screening = Screening(screeningId, movieId, LocalDateTime.parse("2024-12-01T20:00:00"), 15_00)

        // when
        val result = transformScreeningToScreeningResponse(screening)

        // then
        assertEquals(LocalDateTime.parse("2024-12-01T20:00:00"), result.timestamp)
        assertEquals(15_00, result.price)
    }
}
