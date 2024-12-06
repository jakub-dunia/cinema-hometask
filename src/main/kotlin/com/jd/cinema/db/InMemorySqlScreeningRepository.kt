package com.jd.cinema.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

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
