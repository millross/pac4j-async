package org.pac4j.async.vertx.handler.impl;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.logic.AsyncCallbackLogic;
import org.pac4j.async.core.logic.DefaultAsyncCallbackLogic;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.async.vertx.http.DefaultHttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public class VertxAsyncCallbackHandler implements Handler<RoutingContext> {

    protected static final Logger LOG = LoggerFactory.getLogger(VertxAsyncCallbackHandler.class);

    private final AsyncCallbackLogic<Void, CommonProfile, VertxAsyncWebContext> callbackLogic;
    protected final VertxAsynchronousComputationAdapter asynchronousComputationAdapter;

    // Config elements which are all optional
    private final String defaultUrl;


    public  VertxAsyncCallbackHandler(final Vertx vertx,
                                     final Context context,
                                     final AsyncConfig<Void, CommonProfile, VertxAsyncWebContext> config,
                                     final CallbackHandlerOptions options) {

        callbackLogic = new DefaultAsyncCallbackLogic<>(options.getMultiProfile(),
                options.getRenewSession(),
                config,
                new DefaultHttpActionAdapter());
        this.defaultUrl = options.getDefaultUrl();
        this.asynchronousComputationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);

    }

    @Override
    public void handle(RoutingContext event) {

        final VertxAsyncWebContext webContext = new VertxAsyncWebContext(event, asynchronousComputationAdapter);

        callbackLogic.perform(webContext, defaultUrl)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        event.fail(failure);
                    }
        });
    }
}
