package com.main

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

object KotlinScriptRunner {
    suspend fun runScript(call: ApplicationCall, code: String) {
        val tempFile = File.createTempFile("script_${UUID.randomUUID()}", ".kts")

        try {
            tempFile.writeText(code)

            val process = ProcessBuilder("kotlinc", "-script", tempFile.absolutePath)
                .apply {
                    environment().remove("JAVA_TOOL_OPTIONS")
                }
                .redirectErrorStream(true)
                .start()

            call.respondTextWriter(ContentType.Text.Plain) {
                try {
                    withTimeout(120000) {
                        val reader = BufferedReader(InputStreamReader(process.inputStream))

                        withContext(Dispatchers.IO) {
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                write(line + "\n")
                                flush()
                            }
                        }

                        val completed = process.waitFor(60, TimeUnit.SECONDS)
                        if (!completed) {
                            process.destroyForcibly()
                            write("\n[Process timed out and was terminated]\n")
                            flush()
                        } else {
                            val exitCode = process.exitValue()
                            if (exitCode != 0) {
                                write("\n[Process exited with code $exitCode]\n")
                                flush()
                            }
                        }
                    }
                } catch (_: TimeoutCancellationException) {
                    process.destroyForcibly()
                    write("\n[Execution timed out after 120 seconds]\n")
                    flush()
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            try {
                call.respondTextWriter(ContentType.Text.Plain) {
                    write("Error executing script: ${e.message}\n")
                    flush()
                }
            } finally {
                tempFile.delete()
            }
        }
    }
}

