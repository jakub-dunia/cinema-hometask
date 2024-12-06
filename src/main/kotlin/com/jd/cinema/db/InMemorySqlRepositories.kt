package com.jd.cinema.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class InMemorySqlMovieRepository(database: Database) : MovieRepository {

    object DbMovies : Table() {
        val id = varchar("id", length = 36)
        val title = varchar("title", length = 500)
        val externalId = varchar("external_id", length = 500)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(DbMovies)

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
            ).forEach {
                create(it)
            }
        }
    }

    override fun fetchAllMovies(): List<Movie> {
        return runBlocking {
            dbQuery {
                DbMovies.selectAll()
                    .map { Movie(UUID.fromString(it[DbMovies.id]), it[DbMovies.title], it[DbMovies.externalId]) }
            }
        }
    }

    override fun fetchMovie(movieId: UUID): Movie? {
        return runBlocking {
            dbQuery {
                DbMovies.selectAll()
                    .where { DbMovies.id eq movieId.toString() }
                    .map { Movie(UUID.fromString(it[DbMovies.id]), it[DbMovies.title], it[DbMovies.externalId]) }
                    .singleOrNull()
            }
        }
    }

    fun create(movie: Movie): String = runBlocking {
        dbQuery {
            DbMovies.insert {
                it[id] = movie.id.toString()
                it[title] = movie.title
                it[externalId] = movie.externalId
            }[DbMovies.id]
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

}

class InMemorySqlScreeningRepository(database: Database) : ScreeningRepository {

    object DbScreening : Table() {
        val id = varchar("id", length = 36)
        val screeningMovieId = varchar("movie_id", length = 36)
        val screeningTime = varchar("screening_time", length = 25)
        val screeningPrice = integer("price")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(DbScreening)
        }
    }

    override fun fetchScreeningsByMovieId(movieId: UUID): Set<Screening> {
        return runBlocking {
            dbQuery {
                DbScreening.selectAll()
                    .where { DbScreening.screeningMovieId eq movieId.toString() }
                    .map {
                        Screening(
                            UUID.fromString(it[DbScreening.id]),
                            UUID.fromString(it[DbScreening.screeningMovieId]),
                            LocalDateTime.parse(it[DbScreening.screeningTime], LocalDateTimeSerializer.formatter),
                            it[DbScreening.screeningPrice]
                        )
                    }
                    .toSet()
            }
        }
    }

    override fun addScreening(movieId: UUID, timestamp: LocalDateTime, price: Int): Screening {
        return runBlocking {

            val existing =
                fetchScreeningsByMovieId(movieId).firstOrNull { it.timestamp == timestamp }

            if (existing != null)
                return@runBlocking existing

            val screening = Screening(UUID.randomUUID(), movieId, timestamp, price)

            val rows = dbQuery {
                DbScreening.insert {
                    it[id] = screening.id.toString()
                    it[screeningMovieId] = screening.movieId.toString()
                    it[screeningTime] = LocalDateTimeSerializer.formatter.format(screening.timestamp)
                    it[screeningPrice] = screening.price
                }
            }

            if (rows.insertedCount == 1)
                return@runBlocking screening
            else
                throw NoSuchElementException("Could not add screening: $screening")
        }
    }

    override fun deleteScreening(movieId: UUID, timestamp: LocalDateTime): Boolean {
        return runBlocking {
            dbQuery {
                DbScreening.deleteWhere {
                    DbScreening.screeningMovieId eq movieId.toString() and
                            (
                                    DbScreening.screeningTime eq timestamp.format(
                                        LocalDateTimeSerializer.formatter
                                    ))
                } > 0
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

}

class InMemorySqlReviewRepository(database: Database) : ReviewRepository {

    object DbReview : Table() {
        val id = varchar("id", length = 36)
        val reviewedMovieId = varchar("movie_id", length = 36)
        val reviewTimestamp = varchar("timestamp", length = 25)
        val movieRating = integer("rating")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(DbReview)
        }
    }

    override fun addReview(movieId: UUID, rating: Int, timestamp: LocalDateTime): Review {
        return runBlocking {
            val review = Review(UUID.randomUUID(), movieId, rating, timestamp)
            dbQuery {
                DbReview.insert {
                    it[id] = review.id.toString()
                    it[reviewedMovieId] = review.movieId.toString()
                    it[reviewTimestamp] = LocalDateTimeSerializer.formatter.format(review.timestamp)
                    it[movieRating] = review.rating
                }
            }
            return@runBlocking review
        }
    }

    override fun fetchAllReviews(movieId: UUID): List<Review> {
        return runBlocking {
            dbQuery {
                DbReview.selectAll()
                    .where { DbReview.reviewedMovieId eq movieId.toString() }
                    .map {
                        Review(
                            UUID.fromString(it[DbReview.id]),
                            UUID.fromString(it[DbReview.reviewedMovieId]),
                            it[DbReview.movieRating],
                            LocalDateTime.parse(it[DbReview.reviewTimestamp], LocalDateTimeSerializer.formatter)
                        )
                    }
            }
        }
    }

    override fun getAverageRating(movieId: UUID): Double {
        return runBlocking {
            dbQuery {
                DbReview.selectAll()
                    .where { DbReview.reviewedMovieId eq movieId.toString() }
                    .map { it[DbReview.movieRating] }
                    .average()
            }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}