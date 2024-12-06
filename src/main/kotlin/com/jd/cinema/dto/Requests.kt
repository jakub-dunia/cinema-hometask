package com.jd.cinema.dto

import com.jd.cinema.db.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ReviewRequest(val movieId: String, val rating: Int)

@Serializable
data class ScreeningRequest(val movieId: String, @Serializable(with = LocalDateTimeSerializer::class) val dateTime: LocalDateTime, val price: Int)

@Serializable
data class ScreeningDeleteRequest(val movieId: String, @Serializable(with = LocalDateTimeSerializer::class) val dateTime: LocalDateTime)