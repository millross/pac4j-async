package org.pac4j.async.vertx

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.awaitResult
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

        val router = Router.router(vertx)
        with(router) {

        }
        LOG.info("Starting server")
        awaitResult<HttpServer> { vertx.createHttpServer().requestHandler(router::accept).listen(9292, it) }
        LOG.info("Server started")

    }


}