package com.jd.cinema.db

import java.time.LocalDateTime
import java.util.*

data class Movie(val id: UUID, val title: String, val externalId: String)

data class Screening(val id: UUID, val movieId: UUID, val timestamp: LocalDateTime, val price: Int)

data class Review(val id: UUID, val movieId: UUID, val rating: Int, val timestamp: LocalDateTime)// idea - add deviceId for some simple unique check and cookie based spam prevention

interface MovieRepository {

    fun fetchAllMovies() : List<Movie>
}

interface ScreeningRepository {

    fun fetchScreeningsByMovieId(movieId: UUID) : Set<Screening>

    fun fetchScreenings(movieId: UUID) : List<Screening>

    fun addScreening(movieId: UUID, timestamp: LocalDateTime, price: Int) : Screening

    fun deleteScreening(movieId: UUID, timestamp: LocalDateTime) : Boolean
}

interface ReviewRepository {

    fun addReview(movieId: UUID, rating: Int, timestamp: LocalDateTime) : Review

    fun fetchAllReviews(movieId: UUID) : List<Review>

    fun getAverageRating(movieId: UUID) : Double
}