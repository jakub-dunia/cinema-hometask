package com.jd.cinema

import com.jd.cinema.db.*
import com.jd.cinema.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime
import java.util.*

/**
 *     An internal endpoint in which they (i.e., the cinema owners) can update show times and prices for their movie catalog
 *
 *     An endpoint in which their customers (i.e., moviegoers) can fetch movie times
 *
 *     An endpoint in which their customers (i.e., moviegoers) can fetch details about one of their movies (e.g., name, description, release date, rating, IMDb rating, and runtime). Even though there's a limited offering, please use the OMDb APIs (detailed below) to demonstrate how to communicate across APIs.
 *
 *     An endpoint in which their customers (i.e., moviegoers) can leave a review rating (from 1-5 stars) about a particular movie
 *
 */

fun Application.configureRouting() {

    val movieRepository: MovieRepository =
        InMemoryMovieRepository.InMemoryMovieRepository.createFastAndFuriousDatabase()
    val screeningRepository: ScreeningRepository = InMemoryScreeningRepository(mutableMapOf())
    val reviewRepository: ReviewRepository = InMemoryReviewRepository()

    routing {
        route("/int/v1/") {
            route("screenings") {
                get {
                    val movieId = UUID.fromString(call.queryParameters["movieId"].orEmpty())

                    // bad request on invalid movie id

                    val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)

                    call.respond(MovieResponse(movieId, screenings.map { ScreeningResponse(it.timestamp, it.price) }))
                }
                put {
                    val screeningRequest: ScreeningRequest = call.receive<ScreeningRequest>()

                    // add validation if movie existing, if screening in the future? if price > 0

                    screeningRepository.addScreening(
                        UUID.fromString(screeningRequest.movieId),
                        screeningRequest.dateTime,
                        screeningRequest.price
                    )

                    call.respond(HttpStatusCode.NoContent)
                }
                delete {
                    val deleteRequest: ScreeningDeleteRequest = call.receive<ScreeningDeleteRequest>()

                    screeningRepository.deleteScreening(UUID.fromString(deleteRequest.movieId), deleteRequest.dateTime)

                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        route("/ext/v1/") {
            route("movies") {
                get {
                    //TODO potentially not needed
                }
                route("/{movieId}") {
                    get {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        // bad request on invalid movie id

                        val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)

                        call.respond(MovieResponse(movieId, screenings.map { ScreeningResponse(it.timestamp, it.price) }, null)) // enrich with movie details

                    }
                    get("/screenings") {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)

                        call.respond(MovieScreeningResponse(movieId, screenings.map { ScreeningResponse(it.timestamp, it.price) }))
                    }
                    get("/details") {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])


                        call.respond(MovieDetailsResponse(movieId, "stub", "stub", "stub"))

                    }
                    get("/rating") {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        call.respond(ReviewSummaryResponse(movieId, reviewRepository.getAverageRating(movieId)))
                    }

                }

            }
            route("reviews") {
                // not in spec, debug endpoint
                get {
                    val movieId = UUID.fromString(call.queryParameters["movieId"].orEmpty())// invalid value handling
                    reviewRepository.fetchAllReviews(movieId)
                }

                post {
                    val reviewRequest: ReviewRequest = call.receive<ReviewRequest>()

                    // validation of request between 1 and 5

                    reviewRepository.addReview(
                        UUID.fromString(reviewRequest.movieId),
                        reviewRequest.rating,
                        LocalDateTime.now()
                    )

                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}
