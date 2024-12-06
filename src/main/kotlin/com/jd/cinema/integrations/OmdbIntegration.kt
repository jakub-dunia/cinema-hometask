package com.jd.cinema.integrations

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val OMDB_API_KEY = System.getenv("OMDB_KEY")

/**
 * Assuming few fields to prove serialization is working, is enough as PoC
 */
@Serializable
data class OmdbResponse(
    val Title: String = "",
    val Year: String = "",
    val Rated: String = "",
    val Released: String = "",
    val Runtime: String = "",
    val Genre: String = "",
    val Director: String = "",
    val Writer: String = "",
    val Actors: String = "",
    val Plot: String = "",
)

/**
 * Could abstract even further and go with MovieDetailsVendorIntegration, and have response structure to be vendor independent,
 * but it feels like overkill at this scale.
 */
interface OmdbIntegration {

    fun getMovieDetails(imdbId: String): OmdbResponse

}

class OmdbHttpIntegration : OmdbIntegration {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    override fun getMovieDetails(imdbId: String): OmdbResponse = runBlocking {
        val response: HttpResponse = client.get("http://www.omdbapi.com/?apikey=${OMDB_API_KEY}&i=${imdbId}")
        return@runBlocking response.body<OmdbResponse>()
    }
}