package org.pac4j.async.vertx.handler.impl;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.logic.AsyncLogoutLogic;
import org.pac4j.async.core.logic.DefaultAsyncLogoutLogic;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.async.vertx.VertxAsyncProfileManager;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.async.vertx.http.DefaultHttpActionAdapter;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public class VertxAsyncLogoutHandler implements Handler<RoutingContext> {

    protected final AsyncConfig<Void, CommonProfile, VertxAsyncWebContext> config;
    private final AsyncLogoutLogic<Void, VertxAsyncWebContext> logoutLogic;
    private final Vertx vertx;
    private final AsyncSessionStore sessionStore;
    protected final VertxAsynchronousComputationAdapter asynchronousComputationAdapter;


    protected HttpActionAdapter<Void, VertxAsyncWebContext> httpActionAdapter = new DefaultHttpActionAdapter();

    public VertxAsyncLogoutHandler(final Vertx vertx,
                                   final Context context,
                                   final AsyncSessionStore sessionStore,
                                   final AsyncConfig<Void, CommonProfile, VertxAsyncWebContext> config,
                                   final LogoutHandlerOptions options) {
        DefaultAsyncLogoutLogic<Void, CommonProfile, VertxAsyncWebContext> defaultApplicationLogoutLogic = new DefaultAsyncLogoutLogic<>(config,
                httpActionAdapter,
                options.getDefaultUrl(),
                options.getLogoutUrlPattern(),
                options.isLocalLogout(),
                options.isDestroySession(),
                options.isCentralLogout());
        defaultApplicationLogoutLogic.setProfileManagerFactory(c -> new VertxAsyncProfileManager(c));
        this.logoutLogic = defaultApplicationLogoutLogic;
        this.config = config;
        this.vertx = vertx;
        this.sessionStore = sessionStore;
        this.asynchronousComputationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);
    }

    @Override
    public void handle(RoutingContext event) {
        final VertxAsyncWebContext webContext = new VertxAsyncWebContext(event, asynchronousComputationAdapter, sessionStore);

        logoutLogic.perform(webContext)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        event.fail(failure);
                    }
                });

    }
}
