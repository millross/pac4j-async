package org.pac4j.async.vertx

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.awaitResult
import org.pac4j.async.vertx.auth.Pac4jAuthProvider
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Class to represent the test server for testing of the vert.x async implementation
 */
class TestServer(val vertx: Vertx) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(TestServer.javaClass)

        fun getProfileManager(rc: RoutingContext,
                              asynchronousComputationAdapter: VertxAsynchronousComputationAdapter): VertxAsyncProfileManager<VertxAsyncWebContext> {
            val webContext = VertxAsyncWebContext(rc, asynchronousComputationAdapter)
            return VertxAsyncProfileManager(webContext)

        }

        fun spoofLoginHandler(vertx: Vertx): Handler<RoutingContext> {
            return Handler { rc: RoutingContext ->
                AsyncSecurityHandlerTest.LOG.info("Spoof login endpoint called")
                AsyncSecurityHandlerTest.LOG.info("Session id = " + rc.session().id())

                // Set up a pac4j user and save into the session, we can interrogate later
                val asynchronousComputationAdapter = VertxAsynchronousComputationAdapter(vertx, vertx.orCreateContext)
                val profileManager = getProfileManager(rc, asynchronousComputationAdapter)
                val profile = TestProfile.from(TEST_CREDENTIALS)
                profileManager.save(true, profile, false)

                AsyncSecurityHandlerTest.LOG.info("Spoof login endpoint completing")
                rc.response().setStatusCode(204).end()
            }
        }
    }


    suspend fun start() {

        val router = Router.router(vertx)
        val sessionStore = LocalSessionStore.create(vertx)
        val authProvider = Pac4jAuthProvider()

        with(router) {

            route().handler(CookieHandler.create())
            route().handler(SessionHandler.create(sessionStore))
            route().handler(UserSessionHandler.create(authProvider))
            route().handler(BodyHandler.create())

            post("/spoofLogin").handler(spoofLoginHandler(vertx))

            get().handler {
                LOG.info("Hello, world")
                it.response().end("Hello, world")
            }
        }
        AsyncSecurityHandlerTest.LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(8080, it) }
        AsyncSecurityHandlerTest.LOG.info("Server started")

    }


}