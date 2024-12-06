package com.jd.cinema

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "ktor.credentials.admin_user" to "test",
                "ktor.credentials.admin_password" to "test",
                "ktor.credentials.db_user" to "test",
                "ktor.credentials.db_password" to "test"
            )
        }

        application {
            configureSerialization()
            configureRouting()
        }
        client.get("/openapi").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
