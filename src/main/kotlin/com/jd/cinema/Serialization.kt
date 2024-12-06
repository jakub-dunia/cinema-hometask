package com.jd.cinema

import com.google.gson.*
import com.jd.cinema.db.LocalDateTimeSerializer
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import java.lang.reflect.Type
import java.time.LocalDateTime


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(
                LocalDateTime::class.java, GsonLocalDateTimeSerializer()
            )
        }
    }
}

class GsonLocalDateTimeSerializer : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    override fun serialize(value: LocalDateTime, type: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(value.format(LocalDateTimeSerializer.formatter))
    }

    override fun deserialize(json: JsonElement, type: Type?, context: JsonDeserializationContext?): LocalDateTime {
        return LocalDateTime.parse(
            json.asString,
            LocalDateTimeSerializer.formatter
        )
    }

}
