package com.jd.cinema.db

import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MovieTest {

    @Test
    fun shouldGetValidImdbId() {
        // given
        val movie = Movie(UUID.randomUUID(), "title", "imdb:someid")

        // when
        val imdbId = movie.getImdbId()

        // then
        assertEquals("someid", imdbId)
    }

    @Test
    fun shouldGetNullOnEmptyString() {
        // given
        val movie = Movie(UUID.randomUUID(), "title", "")

        // when
        val imdbId = movie.getImdbId()

        // then
        assertNull(imdbId)
    }

    @Test
    fun shouldGetNullOnDifferentVendor() {
        // given
        val movie = Movie(UUID.randomUUID(), "title", "netflix:someid")

        // when
        val imdbId = movie.getImdbId()

        // then
        assertNull(imdbId)
    }
}

class ScreeningTest {

    @Test
    fun shouldNotAcceptNegativePrice() {
        assertThrows<IllegalArgumentException> {
            Screening(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                -5
            )
        }
    }
}

class ReviewTest {

    @Test
    fun shouldNotAcceptRatingBelow1() {
        assertThrows<IllegalArgumentException> {
            Review(
                UUID.randomUUID(),
                UUID.randomUUID(),
                0,
                LocalDateTime.now()
            )
        }
    }

    @Test
    fun shouldNotAcceptRatingAbove5() {
        assertThrows<IllegalArgumentException> {
            Review(
                UUID.randomUUID(),
                UUID.randomUUID(),
                6,
                LocalDateTime.now()
            )
        }
    }
}