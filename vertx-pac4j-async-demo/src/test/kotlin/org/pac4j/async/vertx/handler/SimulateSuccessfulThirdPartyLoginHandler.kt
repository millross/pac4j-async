package org.pac4j.async.vertx.handler

import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.experimental.launch
import org.pac4j.async.vertx.PROTECTED_URL
import org.pac4j.async.vertx.STATE_SESSION_PARAMETER
import org.pac4j.async.vertx.STATE_VALUE
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter
import org.pac4j.async.vertx.context.VertxAsyncWebContext
import org.pac4j.async.vertx.core.session.VertxAsyncSessionStore
import org.pac4j.core.context.Pac4jConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class SimulateSuccessfulThirdPartyLoginHandler(val vertx: Vertx, context: Context): Handler<RoutingContext> {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SimulateSuccessfulThirdPartyLoginHandler.javaClass)
    }

    val asynchronousComputationAdapter = VertxAsynchronousComputationAdapter(vertx, context)

    override fun handle(rc: RoutingContext) {

        LOG.info("Simulate third party login endpoint called")
        LOG.info("Session id = " + rc.session().id())

        launch {
            val webContext = VertxAsyncWebContext(rc, asynchronousComputationAdapter)
            // Set up session with the following:
            // (1) Originally requested url
            val sessionStore = webContext.getSessionStore<VertxAsyncSessionStore>()
            sessionStore.set(webContext,
                    Pac4jConstants.REQUESTED_URL, PROTECTED_URL)
            // (2) Known state parameter
            sessionStore.set(webContext, STATE_SESSION_PARAMETER, STATE_VALUE)

            rc.response().setStatusCode(204).end()
            LOG.info("Simulate third party login endpoint completing")

        }
    }

}