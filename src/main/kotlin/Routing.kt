package com.main

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        post("/validate-code") {
            call.receive<String>()
            call.respondText("not implemented")
        }
        get("/run-code") {
            call.response.cacheControl(CacheControl.NoCache(null))
            call.respondTextWriter {
                write("Streaming response from run-code endpoint")
                flush()
            }
        }
    }
}
