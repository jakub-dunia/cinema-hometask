package com.jd.cinema.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
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

    private fun create(movie: Movie): String = runBlocking {
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