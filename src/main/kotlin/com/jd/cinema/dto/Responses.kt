package com.jd.cinema.dto

import com.jd.cinema.db.Movie
import java.time.LocalDateTime
import java.util.*

data class MovieResponse(val id: UUID, val screenings: List<ScreeningResponse>, val details: MovieDetailsResponse? = null)

data class MovieDetailsResponse(val id: UUID, val title: String, val year: String, val rated: String) // more to follow

data class MovieScreeningResponse(val movieId: UUID, val screenings: List<ScreeningResponse>)

data class ScreeningResponse(val timestamp: LocalDateTime, val price: Int)

data class ReviewSummaryResponse(val movieId: UUID, val rating: Double)

/*fun transformMovieToResponse(movie: Movie): MovieResponse {
    return MovieResponse(movie.id, movie.title)
}*/

