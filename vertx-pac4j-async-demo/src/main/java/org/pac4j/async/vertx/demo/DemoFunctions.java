package org.pac4j.async.vertx.demo;

import io.vertx.ext.web.RoutingContext;
import org.pac4j.async.vertx.VertxAsyncProfileManager;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;

/**
 *
 */
public class DemoFunctions {

    public static VertxAsyncProfileManager<VertxAsyncWebContext> getProfileManager(RoutingContext rc,
                                                              VertxAsynchronousComputationAdapter asynchronousComputationAdapter) {
        final VertxAsyncWebContext webContext = new VertxAsyncWebContext(rc, asynchronousComputationAdapter);
        return new VertxAsyncProfileManager<>(webContext);

    }



}
