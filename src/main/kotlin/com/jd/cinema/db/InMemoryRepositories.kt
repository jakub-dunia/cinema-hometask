package com.jd.cinema.db

import java.time.LocalDateTime
import java.util.*


class InMemoryMovieRepository(private val movies: List<Movie>) : MovieRepository {

    override fun fetchAllMovies(): List<Movie> {
        return movies
    }

    object InMemoryMovieRepository {

        fun createFastAndFuriousDatabase(): MovieRepository {
            return InMemoryMovieRepository(
                listOf(
                    Movie(UUID.randomUUID(), "The Fast and the Furious", "imdb:tt0232500"),
                    Movie(UUID.randomUUID(), "2 Fast 2 Furious", "imdb:tt0322259"),
                    Movie(UUID.randomUUID(), "The Fast and the Furious: Tokyo Drift", "imdb:tt0463985"),
                    Movie(UUID.randomUUID(), "Fast & Furious", "imdb:tt1013752"),
                    Movie(UUID.randomUUID(), "Fast Five", "imdb:tt1596343"),
                    Movie(UUID.randomUUID(), "Fast & Furious 6", "imdb:tt1905041"),
                    Movie(UUID.randomUUID(), "Furious 7", "imdb:tt2820852"),
                    Movie(UUID.randomUUID(), "The Fate of the Furious", "imdb:tt4630562"),
                    Movie(UUID.randomUUID(), "F9: The Fast Saga", "imdb:tt5433138")
                )
            )
        }
    }

}

class InMemoryScreeningRepository(private val screenings: MutableMap<UUID, MutableSet<Screening>>) :
    ScreeningRepository {

    override fun fetchScreeningsByMovieId(movieId: UUID): Set<Screening> {
        return screenings[movieId].orEmpty()
    }

    override fun fetchScreenings(movieId: UUID): List<Screening> {
        return screenings.flatMap { it.value }
    }

    override fun addScreening(movieId: UUID, timestamp: LocalDateTime, price: Int): Screening {
        val screening = findScreeningByMovieIdAndTimestamp(movieId, timestamp)
        return if (screening != null)
            screening //TODO better idempotency, we did not update here with new values
        else {
            val movieScreenings: MutableSet<Screening> = screenings[movieId].orEmpty().toMutableSet()

            val newScreening = Screening(UUID.randomUUID(), movieId, timestamp, price)
            movieScreenings.add(newScreening)

            screenings[movieId] = movieScreenings

            return newScreening
        }

    }

    override fun deleteScreening(movieId: UUID, timestamp: LocalDateTime): Boolean {
        val screening = findScreeningByMovieIdAndTimestamp(movieId, timestamp)
        return if (screening != null)
            screenings[movieId]?.remove(screening) ?: false
        else
            false
    }

    private fun findScreeningByMovieIdAndTimestamp(movieId: UUID, timestamp: LocalDateTime): Screening? {
        return screenings[movieId].orEmpty().find { it.timestamp == timestamp }
    }

}

class InMemoryReviewRepository : ReviewRepository {

    private val reviews: MutableMap<UUID, MutableList<Review>> = mutableMapOf()

    override fun fetchAllReviews(movieId: UUID): List<Review> {
        return reviews[movieId].orEmpty()
    }

    override fun addReview(movieId: UUID, rating: Int, timestamp: LocalDateTime): Review {
        val movieReviews: MutableList<Review> = reviews[movieId].orEmpty().toMutableList()
        val review = Review(UUID.randomUUID(), movieId, rating, timestamp)
        movieReviews.add(review)

        reviews[movieId] = movieReviews

        return review
    }

    override fun getAverageRating(movieId: UUID): Double {
        return reviews[movieId].orEmpty().map { it.rating }.average()
    }

}