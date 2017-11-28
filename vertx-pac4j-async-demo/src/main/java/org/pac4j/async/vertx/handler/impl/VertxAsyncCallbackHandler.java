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
import org.pac4j.async.core.session.AsyncSessionStore;
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
    private final AsyncSessionStore sessionStore;
    protected final VertxAsynchronousComputationAdapter asynchronousComputationAdapter;

    // Config elements which are all optional
    private final String defaultUrl;


    public  VertxAsyncCallbackHandler(final Vertx vertx,
                                     final Context context,
                                     final AsyncSessionStore sessionStore,
                                     final Boolean multiProfile,
                                     final Boolean renewSession,
                                     final AsyncConfig<Void, CommonProfile, VertxAsyncWebContext> config,
                                     final String defaultUrl) {

        callbackLogic = new DefaultAsyncCallbackLogic<>(multiProfile,
                renewSession,
                config,
                new DefaultHttpActionAdapter());
        this.defaultUrl = defaultUrl;
        this.sessionStore = sessionStore;
        this.asynchronousComputationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);

    }

    @Override
    public void handle(RoutingContext event) {

        final VertxAsyncWebContext webContext = new VertxAsyncWebContext(event, asynchronousComputationAdapter, sessionStore);

        callbackLogic.perform(webContext, defaultUrl)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        event.fail(failure);
                    }
        });
    }
}
