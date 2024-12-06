package com.jd.cinema.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*


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