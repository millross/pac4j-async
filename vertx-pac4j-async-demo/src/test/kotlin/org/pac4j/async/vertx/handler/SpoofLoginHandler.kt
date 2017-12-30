package org.pac4j.async.vertx.handler

import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.ext.web.RoutingContext
import org.pac4j.async.vertx.*
import org.pac4j.async.vertx.profile.indirect.TestOAuth20Profile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class SpoofLoginHandler(val vertx: Vertx, val context: Context): Handler<RoutingContext> {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SpoofLoginHandler.javaClass)
    }

    override fun handle(rc: RoutingContext) {
        LOG.info("Spoof login endpoint called")
        LOG.info("Session id = " + rc.session().id())

        // Set up a pac4j user and save into the session, we can interrogate later
        val asynchronousComputationAdapter = VertxAsynchronousComputationAdapter(vertx, context)
        val profileManager = getProfileManager(rc, asynchronousComputationAdapter)
        val profile = TestOAuth20Profile.from(TEST_CREDENTIALS)
        profile.addAttribute(EMAIL_KEY, TEST_EMAIL)
        profile.clientName = TEST_CLIENT_NAME
        profileManager.save(true, profile, false)

        LOG.info("Spoof login endpoint completing")
        rc.response().setStatusCode(204).end()
    }
}

