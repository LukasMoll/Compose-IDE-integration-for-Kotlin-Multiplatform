import com.main.module
import com.microsoft.playwright.*
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.Assume.assumeTrue
import java.net.ServerSocket
import java.util.concurrent.TimeUnit
import java.nio.file.Files

class FrontendEnd2EndTest {
    companion object {
        private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>
        private lateinit var baseUrl: String
        private lateinit var playwright: Playwright
        private lateinit var browser: Browser
        @JvmStatic private var kotlincAvailable: Boolean = false

        @JvmStatic
        @BeforeClass
        fun setUpAll() {
            val port = findFreePort()
            baseUrl = "http://127.0.0.1:$port"

            server = embeddedServer(Netty, port = port, host = "127.0.0.1") {
                module()
            }.start(false)

            playwright = Playwright.create()
            browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))

            kotlincAvailable = try {
                val p = ProcessBuilder("kotlinc", "-version")
                    .redirectErrorStream(true)
                    .start()
                p.waitFor(2, TimeUnit.SECONDS) && p.exitValue() == 0
            } catch (_: Exception) { false }
        }

        @JvmStatic
        @AfterClass
        fun tearDownAll() {
            try { server.stop() } catch (_: Exception) {}
            try { browser.close() } catch (_: Exception) {}
            try { playwright.close() } catch (_: Exception) {}
        }

        private fun findFreePort(): Int = ServerSocket(0).use { it.localPort }
    }

    private lateinit var context: BrowserContext
    private lateinit var page: Page

    @Before
    fun createContext() {
        context = browser.newContext()
        page = context.newPage()
    }

    @After
    fun closeContext() {
        try { context.close() } catch (_: Exception) {}
    }

    @Test
    fun testValidCodeValidationUI() {
        page.navigate(baseUrl)
        page.waitForSelector("#codeEditor")
        val code = "fun main() {\n    println(\"Hello\")\n}"
        page.fill("#codeEditor", code)

        val status = page.locator("#errorPanel")
        assertThat(status).containsText("Code is valid")

        val overlay = page.locator("#highlightedCode")
        assertThat(overlay).not().hasText("")
    }

    @Test
    fun testInvalidCodeValidationUI_andClickableError() {
        page.navigate(baseUrl)
        page.waitForSelector("#codeEditor")
        val code = "fun main() { val x = }"
        page.fill("#codeEditor", code)

        val status = page.locator("#errorPanel")
        assertThat(status).containsText("Parsing errors")

        page.click("#errorPanel .error-link")
        val highlight = page.locator("#highlightedCode .error-highlight")
        highlight.waitFor()
        assertThat(highlight).isVisible()
    }

    @Test
    fun testUploadKts_populatesEditor_andValidates() {
        page.navigate(baseUrl)
        page.waitForSelector("#codeEditor")

        val tmp = Files.createTempFile("sample", ".kts")
        val content = "fun main() { println(\"FromFile\") }"
        Files.writeString(tmp, content)

        page.setInputFiles("#fileUpload", tmp)

        assertThat(page.locator("#codeEditor")).hasValue(content)
        assertThat(page.locator("#errorPanel")).containsText("Code is valid")
    }

    @Test
    fun testRunCodeFromUI_streamingOutput() {
        assumeTrue("kotlinc not available, skipping run-code E2E", kotlincAvailable)

        page.navigate(baseUrl)
        page.waitForSelector("#codeEditor")
        page.fill("#codeEditor", "println(\"Hello from Kotlin script\")")
        page.click("#runButton")

        page.waitForSelector("#runResult")
        val runResult = page.locator("#runResult")
        assertThat(runResult).containsText("Hello from Kotlin script")
    }
}
