package com.main

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("/", "")

        get("/") {
            call.respondRedirect("/index.html")
        }

        post("/validate-code") {
            val code = call.receive<String>()
            val result = KotlinParserService.parse(code)
            call.respondText(KotlinParserService.toJson(result), ContentType.Application.Json)
        }

        post("/run-code") {
            val code = call.receive<String>()
            KotlinScriptRunner.runScript(call, code)
        }
    }
}

