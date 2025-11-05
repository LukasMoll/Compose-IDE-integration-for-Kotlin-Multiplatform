package com.main

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
    @Test
    fun testValidateCode() = testApplication {
        application {
            module()
        }
        client.post("/validate-code") {
            contentType(ContentType.Text.Plain)
            setBody("fun main() { println(\"Hello\") }")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = bodyAsText()
            assertTrue(response.contains("\"tokens\""), "Response should contain tokens: $response")
            assertTrue(response.contains("\"errors\""), "Response should contain errors field: $response")
        }
    }

    @Test
    fun testRunCode() = testApplication {
        application {
            module()
        }
        client.post("/run-code") {
            contentType(ContentType.Text.Plain)
            setBody("println(\"Hello from Kotlin script\")")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = bodyAsText()
            assertTrue(response.contains("Hello from Kotlin script"), "Expected script output in response: $response")
        }
    }

    @Test
    fun testValidateCodeWithErrors() = testApplication {
        application {
            module()
        }
        client.post("/validate-code") {
            contentType(ContentType.Text.Plain)
            setBody("fun main() { val x = }")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = bodyAsText()
            assertTrue(response.contains("\"tokens\""), "Response should contain tokens: $response")
            assertTrue(response.contains("\"hasErrors\":true"), "Response should indicate errors: $response")
            assertTrue(response.contains("\"errorLocations\""), "Response should contain errorLocations: $response")
        }
    }
}
