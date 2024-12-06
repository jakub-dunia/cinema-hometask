package com.jd.cinema.dto

import com.jd.cinema.db.LocalDateTimeSerializer
import com.jd.cinema.db.Movie
import com.jd.cinema.db.Screening
import com.jd.cinema.integrations.OmdbResponse
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

data class MoviesResponse(val movies: List<MovieResponse>)

data class MovieResponse(
    val id: UUID,
    val screenings: List<ScreeningResponse>,
    val details: MovieDetailsResponse? = null
)

data class MovieDetailsResponse(val title: String?, val year: String?, val rated: String?) // more to follow

data class MovieScreeningResponse(val movieId: UUID, val screenings: List<ScreeningResponse>)

data class ScreeningResponse(
    @Serializable(with = LocalDateTimeSerializer::class) val timestamp: LocalDateTime,
    val price: Int
)

data class ReviewSummaryResponse(val movieId: UUID, val rating: Double)

fun transformOmdbResponseToMovieDetailsResponse(omdbResponse: OmdbResponse?): MovieDetailsResponse {
    return MovieDetailsResponse(
        omdbResponse?.Title ?: "",
        omdbResponse?.Year ?: "",
        omdbResponse?.Rated ?: ""
        // skipping rest of parameters, this is sufficient as PoC
    )
}

fun transformMovieToMovieResponse(
    movie: Movie,
    screenings: Set<Screening>,
    details: OmdbResponse? = null
): MovieResponse {
    return MovieResponse(
        movie.id,
        screenings.map { transformScreeningToScreeningResponse(it) },
        transformOmdbResponseToMovieDetailsResponse(details)
    )
}

fun transformScreeningToScreeningResponse(screening: Screening): ScreeningResponse {
    return ScreeningResponse(
        screening.timestamp,
        screening.price
    )
}


