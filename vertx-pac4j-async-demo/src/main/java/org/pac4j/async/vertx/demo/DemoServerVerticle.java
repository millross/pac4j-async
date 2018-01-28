package org.pac4j.async.vertx.demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.oauth.client.AsyncFacebookClient;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.auth.Pac4jAuthProvider;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.async.vertx.demo.handler.IndexHandler;
import org.pac4j.async.vertx.demo.handler.PrivateEndpointHandler;
import org.pac4j.async.vertx.demo.handler.SetContentTypeHandler;
import org.pac4j.async.vertx.handler.impl.CallbackHandlerOptions;
import org.pac4j.async.vertx.handler.impl.LogoutHandlerOptions;
import org.pac4j.async.vertx.handler.impl.SecurityHandlerOptions;
import org.pac4j.async.vertx.handler.impl.VertxAsyncCallbackHandler;
import org.pac4j.async.vertx.handler.impl.VertxAsyncLogoutHandler;
import org.pac4j.async.vertx.handler.impl.VertxAsyncSecurityHandler;
import org.pac4j.core.client.Clients;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public class DemoServerVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DemoServerVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        final Context context = vertx.getOrCreateContext();
        final VertxAsynchronousComputationAdapter computationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);

        SessionStore sessionStore = LocalSessionStore.create(vertx);
        AuthProvider authProvider = new Pac4jAuthProvider();

        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(sessionStore));
        router.route().handler(UserSessionHandler.create(authProvider));
        router.route().handler(BodyHandler.create());


        router.get("/").handler(new SetContentTypeHandler(HttpHeaders.TEXT_HTML));
        router.get("/").handler(new IndexHandler(computationAdapter));

        final String callbackUrl = config().getString("baseUrl") + "/callback";
        SecurityHandlerOptions options = new SecurityHandlerOptions().setClients("FacebookClient");
        AsyncFacebookClient asyncFacebookClient = new AsyncFacebookClient(config().getString("fbId"), config().getString("fbSecret"));
        asyncFacebookClient.setName("FacebookClient");
        final Clients<AsyncClient, AsyncAuthorizationGenerator<CommonProfile>> clients = new Clients<>(callbackUrl, asyncFacebookClient);;
        final AsyncConfig<Void, CommonProfile, VertxAsyncWebContext> config = new AsyncConfig<>();
        config.setClients(clients);

        final Pac4jAuthProvider pac4jAuthProvider = new Pac4jAuthProvider();
        router.get("/facebook/index.html").handler(new VertxAsyncSecurityHandler(vertx, context, config,
                pac4jAuthProvider, options));
        router.get("/facebook/index.html").handler(new SetContentTypeHandler(HttpHeaders.TEXT_HTML));
        router.get("/facebook/index.html").handler(new PrivateEndpointHandler(computationAdapter, (rc, buf) -> rc.response().end(buf)));

        CallbackHandlerOptions callbackHandlerOptions = new CallbackHandlerOptions().setDefaultUrl("/");
        VertxAsyncCallbackHandler callbackHandler = new VertxAsyncCallbackHandler(vertx, context, config, callbackHandlerOptions);
        router.get("/callback").handler(callbackHandler); // This will deploy the callback handler

        final VertxAsyncLogoutHandler logoutHandler = new VertxAsyncLogoutHandler(vertx, context, config, new LogoutHandlerOptions().setDefaultUrl("/"));
        router.get("/logout").handler(logoutHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);

    }

}
