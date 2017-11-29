package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.experimental.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pac4j.async.vertx.auth.Pac4jAuthProvider
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
        val router = Router.router(vertx)
        val sessionStore = LocalSessionStore.create(vertx)
        val authProvider = Pac4jAuthProvider()

        with(router) {

            route().handler(CookieHandler.create())
            route().handler(SessionHandler.create(sessionStore))
            route().handler(UserSessionHandler.create(authProvider))
            route().handler(BodyHandler.create())

            get().handler {
                println("Hello, world")
                it.response().end("Hello, world")
            }
        }
        LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(8080, it) }
        LOG.info("Server started")

    }
}
