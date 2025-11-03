package com.main

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

object KotlinScriptRunner {
    suspend fun runScript(call: ApplicationCall, code: String) {
        val tempFile = File.createTempFile("script_${UUID.randomUUID()}", ".kts")

        try {
            tempFile.writeText(code)

            val process = ProcessBuilder("kotlinc", "-script", tempFile.absolutePath)
                .redirectErrorStream(true)
                .start()

            call.respondTextWriter(ContentType.Text.Plain) {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    write(line + "\n")
                    flush()
                }

                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    write("\n[Process exited with code $exitCode]\n")
                    flush()
                }
            }
        } catch (e: Exception) {
            call.respondTextWriter(ContentType.Text.Plain) {
                write("Error executing script: ${e.message}\n")
                flush()
            }
        } finally {
            tempFile.delete()
        }
    }
}

