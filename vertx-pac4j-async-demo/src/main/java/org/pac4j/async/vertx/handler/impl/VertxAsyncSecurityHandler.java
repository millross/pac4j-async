package org.pac4j.async.vertx.handler.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.logic.AsyncSecurityLogic;
import org.pac4j.async.core.logic.DefaultAsyncSecurityLogic;
import org.pac4j.async.vertx.VertxAsyncProfileManager;
import org.pac4j.async.vertx.VertxAsynchronousComputationAdapter;
import org.pac4j.async.vertx.auth.Pac4jAuthProvider;
import org.pac4j.async.vertx.auth.Pac4jUser;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.async.vertx.http.DefaultHttpActionAdapter;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class VertxAsyncSecurityHandler<U extends CommonProfile> extends AuthHandlerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(VertxAsyncSecurityHandler.class);

    protected final Config config;

    protected final String clientNames;
    protected final String authorizerName;
    protected final String matcherName;
    protected final boolean multiProfile;
    protected final Context context;
    protected final VertxAsynchronousComputationAdapter asynchronousComputationAdapter;
    protected final Vertx vertx;

    protected final HttpActionAdapter<Void, VertxAsyncWebContext> httpActionAdapter = new DefaultHttpActionAdapter();

    private final AsyncSecurityLogic<Void, VertxAsyncWebContext> securityLogic;

    public VertxAsyncSecurityHandler(final Vertx vertx,
                                     final Context context,
                                     final AsyncConfig config, final Pac4jAuthProvider authProvider,
                                     final SecurityHandlerOptions options) {
        super(authProvider);
        CommonHelper.assertNotNull("vertx", vertx);
        CommonHelper.assertNotNull("context", context);
        CommonHelper.assertNotNull("config", config);
        CommonHelper.assertNotNull("config.getClients()", config.getClients());
        CommonHelper.assertNotNull("authProvider", authProvider);
        CommonHelper.assertNotNull("options", options);

        clientNames = options.getClients();
        authorizerName = options.getAuthorizers();
        matcherName = options.getMatchers();
        multiProfile = options.isMultiProfile();
        this.vertx = vertx;
        this.asynchronousComputationAdapter = new VertxAsynchronousComputationAdapter(vertx, context);
        this.context = context;
        this.config = config;

        final DefaultAsyncSecurityLogic<Void, U , VertxAsyncWebContext> securityLogic = new DefaultAsyncSecurityLogic<Void, U, VertxAsyncWebContext>(options.isSaveProfileInSession(),
                options.isMultiProfile(), config, httpActionAdapter);
        securityLogic.setProfileManagerFactory(c -> new VertxAsyncProfileManager(c));
        this.securityLogic = securityLogic;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        VertxAsyncWebContext webContext = new VertxAsyncWebContext(routingContext, asynchronousComputationAdapter);
        Pac4jUser pac4jUser = (Pac4jUser)routingContext.user();
        if (pac4jUser != null) {
            Map<String, CommonProfile> indirectProfiles = (Map)pac4jUser.pac4jUserProfiles().entrySet().stream().filter((e) -> {
                String clientName = ((CommonProfile)e.getValue()).getClientName();
                return this.config.getClients().findClient(clientName) instanceof IndirectClient;
            }).collect(Collectors.toMap((e) -> (String)e.getKey(), (e) -> (CommonProfile)e.getValue()));
            if (!indirectProfiles.isEmpty()) {
                pac4jUser.pac4jUserProfiles().clear();
                pac4jUser.pac4jUserProfiles().putAll(indirectProfiles);
            } else {
                routingContext.clearUser();
            }
        }

        securityLogic.perform(webContext, (ctx, parameters) -> {
            LOG.info("Authorised to view resource " + routingContext.request().path());
            routingContext.next();
            return null;
        }, clientNames, authorizerName, matcherName)
        .whenComplete((result, failure) -> {
            vertx.runOnContext(v -> {
                this.unexpectedFailure(routingContext, failure);
            });
        });

    }

    protected void unexpectedFailure(RoutingContext context, Throwable failure) {
        context.fail(this.toTechnicalException(failure));
    }

    protected final TechnicalException toTechnicalException(Throwable t) {
        return t instanceof TechnicalException ? (TechnicalException)t : new TechnicalException(t);
    }

    @Override
    public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
        // Intentional noop - we're letting pac4j deal with credentials and authentication
    }
}
