package org.pac4j.async.vertx.authserver

import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.awaitResult
import org.pac4j.async.vertx.AUTH_SERVER_PROFILE_ENDPOINT
import org.pac4j.async.vertx.AUTH_SERVER_TOKEN_ENDPOINT
import org.pac4j.async.vertx.authserver.handler.ProfileRequestHandler
import org.pac4j.async.vertx.authserver.handler.TokenRequestHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class DummyAuthServer(val vertx: Vertx) {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(DummyAuthServer.javaClass)
    }

    suspend fun start() {

        val context = vertx.orCreateContext
        val router = Router.router(vertx)
        with(router) {
            route(HttpMethod.GET, AUTH_SERVER_TOKEN_ENDPOINT).handler(TokenRequestHandler(vertx, context))
            route(HttpMethod.GET, AUTH_SERVER_PROFILE_ENDPOINT).handler(ProfileRequestHandler(vertx, context))
        }
        LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(9292, it) }
        LOG.info("Server started")

    }


}