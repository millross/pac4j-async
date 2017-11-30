package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.awaitResult
import org.pac4j.async.vertx.auth.Pac4jAuthProvider

/**
 * Class to represent the test server for testing of the vert.x async implementation
 */
class TestServer(val vertx: Vertx) {

    suspend fun start() {

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
        AsyncSecurityHandlerTest.LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(8080, it) }
        AsyncSecurityHandlerTest.LOG.info("Server started")

    }


}