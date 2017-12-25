package org.pac4j.async.vertx

import io.vertx.ext.web.RoutingContext
import org.pac4j.async.vertx.context.VertxAsyncWebContext

/**
 * Functions used by tests
 */
fun getProfileManager(rc: RoutingContext,
                      asynchronousComputationAdapter: VertxAsynchronousComputationAdapter): VertxAsyncProfileManager<VertxAsyncWebContext> {
    val webContext = VertxAsyncWebContext(rc, asynchronousComputationAdapter)
    return VertxAsyncProfileManager(webContext)

}

