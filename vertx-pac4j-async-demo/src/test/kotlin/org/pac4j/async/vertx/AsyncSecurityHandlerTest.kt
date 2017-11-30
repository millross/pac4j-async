package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import kotlinx.coroutines.experimental.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Simple tests for security handler using kotlin as a convenient test mechanism
 */
@RunWith(VertxUnitRunner::class)
class AsyncSecurityHandlerTest {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(AsyncSecurityHandlerTest.javaClass)
    }

    @Rule
    @JvmField
    val rule = RunTestOnContext()

    @Test
    fun testAlreadyLoggedIn(testContext: TestContext) {

        val async = testContext.async()
        val vertx = rule.vertx()
        launch {
            startServer(vertx)
            async.complete()
        }
        async.await()
    }

    suspend fun startServer(vertx: Vertx) {
        TestServer(vertx).start()
    }
}
