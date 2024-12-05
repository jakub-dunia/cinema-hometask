package com.jd.cinema.db

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class Movie(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val title: String,
    val externalId: String
) {

    fun getImdbId(): String {
        return externalId.split(":")[1]//TODO unsafe
    }

}

@Serializable
data class Screening(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val movieId: UUID,
    @Serializable(with = LocalDateTimeSerializer::class) val timestamp: LocalDateTime,
    val price: Int
)

@Serializable
data class Review(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    @Serializable(with = UUIDSerializer::class) val movieId: UUID,
    val rating: Int,
    @Serializable(with = LocalDateTimeSerializer::class) val timestamp: LocalDateTime
)

interface MovieRepository {

    fun fetchAllMovies(): List<Movie>

    fun fetchMovie(movieId: UUID): Movie?
}

interface ScreeningRepository {

    fun fetchScreeningsByMovieId(movieId: UUID): Set<Screening>

    fun addScreening(movieId: UUID, timestamp: LocalDateTime, price: Int): Screening

    fun deleteScreening(movieId: UUID, timestamp: LocalDateTime): Boolean
}

interface ReviewRepository {

    fun addReview(movieId: UUID, rating: Int, timestamp: LocalDateTime): Review

    fun fetchAllReviews(movieId: UUID): List<Review>

    fun getAverageRating(movieId: UUID): Double
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }
}