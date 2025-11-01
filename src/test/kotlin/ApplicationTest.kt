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
            setBody("some code to validate")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("not implemented", bodyAsText())
        }
    }

    @Test
    fun testRunCode() = testApplication {
        application {
            module()
        }
        client.get("/run-code").apply {
            assertEquals(HttpStatusCode.OK, status)
            val response = bodyAsText()
            assertTrue(response.isNotEmpty())
            assertTrue(response.contains("Streaming response from run-code endpoint"))
        }
    }


}
