package com.jd.cinema

import com.jd.cinema.db.*
import com.jd.cinema.dto.*
import com.jd.cinema.integrations.OmdbHttpIntegration
import com.jd.cinema.integrations.OmdbIntegration
import com.jd.cinema.integrations.OmdbResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import java.time.LocalDateTime
import java.util.*

fun Application.configureRouting() {

    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )

    val movieRepository: MovieRepository = InMemorySqlMovieRepository(database)
    val screeningRepository: ScreeningRepository = InMemorySqlScreeningRepository(database)
    val reviewRepository: ReviewRepository = InMemorySqlReviewRepository(database)

    val omdbIntegration: OmdbIntegration = OmdbHttpIntegration()

    fun getMovieDetails(movieId: UUID): OmdbResponse {
        val movie = movieRepository.fetchMovie(movieId)
        return if (movie != null) {
            omdbIntegration.getMovieDetails(movie.getImdbId()!!)
        } else {
            OmdbResponse()
        }
    }

    authentication {
        basic(name = "internal-api-auth") {
            realm = "Ktor Server"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    routing {
        swaggerUI(path = "openapi")

        authenticate("internal-api-auth") {
            route("/int/v1/") {
                route("screenings") {
                    get {
                        try {
                            val movieId = UUID.fromString(
                                call.queryParameters["movieId"]
                                    ?: throw IllegalArgumentException("Movie ID not provided")
                            )

                            movieRepository.fetchMovie(movieId) ?: throw IllegalArgumentException("Movie not found")

                            val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)

                            call.respond(
                                MovieResponse(
                                    movieId,
                                    screenings.map { ScreeningResponse(it.timestamp, it.price) })
                            )
                        } catch (e: IllegalArgumentException) {
                            call.application.environment.log.error(e.message)
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                        }
                    }
                    put {
                        val screeningRequest: ScreeningRequest = call.receive<ScreeningRequest>()
                        try {
                            val movieId = UUID.fromString(screeningRequest.movieId)

                            movieRepository.fetchMovie(movieId) ?: throw IllegalArgumentException("Movie not found")

                            screeningRepository.addScreening(
                                UUID.fromString(screeningRequest.movieId),
                                screeningRequest.dateTime,
                                screeningRequest.price
                            )

                            call.respond(HttpStatusCode.NoContent)
                        } catch (e: IllegalArgumentException) {
                            call.application.environment.log.error(e.message)
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                        }
                    }
                    delete {
                        val deleteRequest: ScreeningDeleteRequest = call.receive<ScreeningDeleteRequest>()

                        screeningRepository.deleteScreening(
                            UUID.fromString(deleteRequest.movieId),
                            deleteRequest.dateTime
                        )

                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        route("/ext/v1/") {
            route("movies") {
                get {
                    call.respond(MoviesResponse(movieRepository.fetchAllMovies().map {
                        transformMovieToMovieResponse(it, screeningRepository.fetchScreeningsByMovieId(it.id))
                    }))
                }
                route("/{movieId}") {
                    get {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        val movie = movieRepository.fetchMovie(movieId)

                        if (movie == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)
                            val movieDetails = omdbIntegration.getMovieDetails(movie.getImdbId()!!)

                            call.respond(
                                MovieResponse(
                                    movieId,
                                    screenings.map { ScreeningResponse(it.timestamp, it.price) },
                                    transformOmdbResponseToMovieDetailsResponse(movieId, movieDetails)
                                )
                            )
                        }

                    }
                    get("/screenings") {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        val screenings = screeningRepository.fetchScreeningsByMovieId(movieId)

                        call.respond(
                            MovieScreeningResponse(
                                movieId,
                                screenings.map { ScreeningResponse(it.timestamp, it.price) })
                        )
                    }
                    get("/details") {
                        val movieId = UUID.fromString(call.pathParameters["movieId"])

                        val movie = movieRepository.fetchMovie(movieId)
                        if (movie == null) {
                            call.respond(HttpStatusCode.NotFound)
                        } else {
                            val movieDetails = omdbIntegration.getMovieDetails(movie.getImdbId()!!)
                            call.respond(
                                transformOmdbResponseToMovieDetailsResponse(movieId, movieDetails)
                            )
                        }


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
