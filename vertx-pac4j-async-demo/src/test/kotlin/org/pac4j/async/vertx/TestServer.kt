package org.pac4j.async.vertx

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.UserSessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import io.vertx.kotlin.coroutines.awaitResult
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.launch
import org.pac4j.async.core.config.AsyncConfig
import org.pac4j.async.core.context.AsyncWebContext
import org.pac4j.async.vertx.auth.Pac4jAuthProvider
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.core.profile.CommonProfile
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

                LOG.info("Spoof login endpoint completing")
                rc.response().setStatusCode(204).end()
            }
        }

        fun getProfileHandler(vertx: Vertx): Handler<RoutingContext> {
            return Handler { rc: RoutingContext ->
                launch {
                    LOG.info("Get profile endpoint called")
                    LOG.info("Session id = " + rc.session().id())
                    val sessionId = rc.session().id()
                    val asynchronousComputationAdapter = VertxAsynchronousComputationAdapter(vertx, vertx.orCreateContext)
                    val profileManager = getProfileManager(rc, asynchronousComputationAdapter)

                    val profile = profileManager.get(true).await()
                    val userId = profile.map { it.id }.orElse(null)
                    val email = profile.map { it.getAttribute(EMAIL_KEY) }.orElse(null)

                    val json = JsonObject()
                            .put(USER_ID_KEY, userId)
                            .put(SESSION_ID_KEY, sessionId)
                            .put(EMAIL_KEY, email)

                    LOG.info("Json is\n" + json.encodePrettily())
                    LOG.info("Get profile endpoint completing")

                    rc.response().end(json.encodePrettily())
                }

            }
        }
    }

    var pac4jConfiguration: AsyncConfig<Void, CommonProfile, AsyncWebContext>? = null

    fun withPac4jConfiguration(config: AsyncConfig<Void, CommonProfile, AsyncWebContext>): TestServer {
        pac4jConfiguration = config
        return this
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
            get("/profile").handler(getProfileHandler(vertx))
        }
        AsyncSecurityHandlerTest.LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(8080, it) }
        AsyncSecurityHandlerTest.LOG.info("Server started")

    }


}