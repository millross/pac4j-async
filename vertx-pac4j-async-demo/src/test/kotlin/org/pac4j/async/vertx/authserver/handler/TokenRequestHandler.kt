package org.pac4j.async.vertx.authserver.handler

import io.vertx.core.Context
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 *
 */
class TokenRequestHandler(val vertx: Vertx, val context: Context): Handler<RoutingContext> {

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(TokenRequestHandler.javaClass)
    }

    override fun handle(rc: RoutingContext) {

        LOG.info("Token request endpoint called")

        val grantType = Optional.ofNullable(rc.request().getParam("grant_type"))
        val code = rc.request().getParam("code")
        val redirectUri = rc.request().getParam("redirect_uri")
        val clientId = rc.request().getParam("client_id")

        val token: Optional<String> = grantType.flatMap { s ->
            if (code == null || redirectUri == null || clientId == null) {
                Optional.empty<String>()
            } else {
                Optional.of(s == "authorization_code").flatMap { b -> if (b) Optional.of(UUID.randomUUID().toString()) else Optional.empty<String>() }
            }
        }

        if (token.isPresent) {
            val responseBody = JsonObject().put("access_token", token.get())
                    .put("token_type", "Bearer")
                    .put("expires_in", 5000)
            rc.response().setStatusCode(200).end(responseBody.toString())
        } else {
            rc.fail(401) // We couldn't resolve to a token
        }
        LOG.info("Token request endpoint completing")
    }

}