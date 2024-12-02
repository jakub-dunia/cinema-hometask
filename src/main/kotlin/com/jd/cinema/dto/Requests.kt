package com.jd.cinema.dto

import java.time.LocalDateTime

data class ReviewRequest(val movieId: String, val rating: Int)

data class ScreeningRequest(val movieId: String, val dateTime: LocalDateTime, val price: Int)

data class ScreeningDeleteRequest(val movieId: String, val dateTime: LocalDateTime)