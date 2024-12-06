package com.jd.cinema.dto

import com.jd.cinema.db.Movie
import com.jd.cinema.db.Screening
import com.jd.cinema.integrations.OmdbResponse
import java.time.LocalDateTime
import java.util.*

data class MoviesResponse(val movies: List<MovieResponse>)

data class MovieResponse(
    val id: UUID,
    val screenings: List<ScreeningResponse>,
    val details: MovieDetailsResponse? = null
)

data class MovieDetailsResponse(val id: UUID, val title: String, val year: String, val rated: String) // more to follow

data class MovieScreeningResponse(val movieId: UUID, val screenings: List<ScreeningResponse>)

data class ScreeningResponse(val timestamp: LocalDateTime, val price: Int)

data class ReviewSummaryResponse(val movieId: UUID, val rating: Double)

fun transformOmdbResponseToMovieDetailsResponse(movieId: UUID, omdbResponse: OmdbResponse?): MovieDetailsResponse {
    return MovieDetailsResponse(
        movieId,
        omdbResponse?.Title ?: "",
        omdbResponse?.Year ?: "",
        omdbResponse?.Rated ?: ""
        // skipping rest of parameters, this is sufficient as PoC
    )
}

fun transformMovieToMovieResponse(movie: Movie, screenings: Set<Screening>, details: OmdbResponse? = null): MovieResponse {
    return MovieResponse(
        movie.id,
        screenings.map { transformScreeningToScreeningResponse(it) },
        transformOmdbResponseToMovieDetailsResponse(movie.id, details)
    )
}

fun transformScreeningToScreeningResponse(screening: Screening): ScreeningResponse {
    return ScreeningResponse(
        screening.timestamp,
        screening.price
    )
}


