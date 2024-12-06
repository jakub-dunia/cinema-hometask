package com.jd.cinema

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Due to lack of time opted for simple smoke tests instead of proper integration tests
 */
class ApplicationTest {

    val applicationConfig = MapApplicationConfig(
        "ktor.credentials.admin_user" to "test",
        "ktor.credentials.admin_password" to "test",
        "ktor.credentials.db_user" to "test",
        "ktor.credentials.db_password" to "test"
    )

    @Test
    fun testRoutes() = testApplication {
        environment {
            config = applicationConfig
        }

        application {
            configureSerialization()
            configureRouting()
        }

        client.get("/openapi").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.get("/ext/v1/movies").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        client.get("/int/v1/screenings").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }

        client.get("/int/v1/screenings") {
            header("Authorization", "Basic dGVzdDp0ZXN0")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)// no movieId provided
        }

        client.get("/int/v1/screenings?movieId=${UUID.randomUUID()}") {
            header("Authorization", "Basic dGVzdDp0ZXN0")
        }.apply {
            assertEquals(HttpStatusCode.NotFound, status)// unknown movie
        }
    }
}
