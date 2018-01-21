package org.pac4j.async.vertx.authserver.handler

import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.pac4j.async.vertx.EMAIL_KEY
import org.pac4j.async.vertx.GOOD_USERNAME
import org.pac4j.async.vertx.TEST_EMAIL
import org.pac4j.async.vertx.USER_ID_KEY
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 */
class ProfileRequestHandler(val vertx: Vertx, val context: Context): Handler<RoutingContext> {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(ProfileRequestHandler.javaClass)
    }

    override fun handle(rc: RoutingContext) {

        LOG.info("Profile request endpoint called")

        val responseBody = JsonObject()
                .put(USER_ID_KEY, GOOD_USERNAME)
                .put(EMAIL_KEY, TEST_EMAIL)
        LOG.info("Token request endpoint completing")
        rc.response().setStatusCode(200).end(responseBody.toString())
    }

}