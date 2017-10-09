package org.pac4j.async.core.logic;

import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.logout.AsyncCentralLogout;
import org.pac4j.async.core.logout.AsyncCentralLogoutStrategy;
import org.pac4j.async.core.logout.AsyncLocalLogout;
import org.pac4j.async.core.logout.AsyncLocalLogoutStrategy;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.destruction.AsyncSessionDestruction;
import org.pac4j.async.core.session.destruction.AsyncSessionDestructionStrategy;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManagerFactoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.pac4j.core.context.Pac4jConstants.DEFAULT_LOGOUT_URL_PATTERN_VALUE;
import static org.pac4j.core.exception.HttpAction.ok;
import static org.pac4j.core.exception.HttpAction.redirect;
import static org.pac4j.core.util.CommonHelper.assertNotBlank;
import static org.pac4j.core.util.CommonHelper.assertNotNull;

/**
 *
 */
public class DefaultAsyncLogoutLogic<R, U extends CommonProfile, WC extends AsyncWebContext>
        extends ProfileManagerFactoryAware<WC, AsyncProfileManager<U, WC>, AsyncConfig<R, U, WC>>
        implements AsyncLogoutLogic<R, WC> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncLogoutLogic.class);

    private final AsyncConfig<R, U, WC> config;
    private final HttpActionAdapter<R, WC> httpActionAdapter;
    private final String defaultUrl;
    private final String logoutUrlPattern;
    private final AsyncLocalLogoutStrategy localLogoutStrategy;
    private final AsyncSessionDestructionStrategy sessionDestructionStrategy;
    private final AsyncCentralLogoutStrategy centralLogoutStrategy;

    public DefaultAsyncLogoutLogic(final AsyncConfig<R, U, WC> config,
                                   final HttpActionAdapter<R, WC> httpActionAdapter,
                                   final String defaultUrl,
                                   final String logoutUrlPattern,
                                   boolean localLogout,
                                   boolean destroySession,
                                   boolean centralLogout) {

        assertNotNull("config", config);
        assertNotNull("clients", config.getClients());
        assertNotNull("httpActionAdapter", httpActionAdapter);
        this.logoutUrlPattern = Optional.ofNullable(logoutUrlPattern).orElse(DEFAULT_LOGOUT_URL_PATTERN_VALUE);
        assertNotBlank(Pac4jConstants.LOGOUT_URL_PATTERN, this.logoutUrlPattern);

        this.config = config;
        this.httpActionAdapter = httpActionAdapter;
        this.defaultUrl = defaultUrl;
        sessionDestructionStrategy = getSessionDestructionStrategy(destroySession);
        this.localLogoutStrategy = getLocalLogoutStrategy(localLogout);
        this.centralLogoutStrategy = getCentralLogoutStrategy(centralLogout);

    }

    @Override
    public CompletableFuture<R> perform(WC context) {
        assertNotNull("context", context);

        // compute redirection URL
        final String url = context.getRequestParameter(Pac4jConstants.URL);
        final String redirectUrl = (url != null && Pattern.matches(logoutUrlPattern, url)) ? url : defaultUrl;

        logger.debug("redirectUrl: {}", redirectUrl);

        final AsyncProfileManager manager = getProfileManager(context, config);

        final CompletableFuture<List<? extends U>> loggedOutProfilesFuture = localLogoutStrategy.logout(manager, sessionDestructionStrategy, context);

        return loggedOutProfilesFuture.thenApply(profiles -> centralLogoutStrategy.getCentralLogoutAction(config.getClients(),
                profiles,
                redirectUrl,
                context))
                .thenApply(o -> o.map(s -> s.get())
                        .orElseGet(() -> redirectUrl == null ? ok("ok", context) : redirect("redirect", context, redirectUrl)))
                .thenApply(action -> httpActionAdapter.adapt(action.getCode(), context));

    }

    @Override
    protected Function<WC, AsyncProfileManager<U, WC>> defaultProfileManagerFactory() {
        return ctx -> new AsyncProfileManager(ctx);
    }

    protected AsyncSessionDestructionStrategy getSessionDestructionStrategy(final boolean destroySession) {
        return destroySession ? AsyncSessionDestruction.DESTROY : AsyncSessionDestruction.DO_NOT_DESTROY;
    }

    protected AsyncLocalLogoutStrategy getLocalLogoutStrategy(final boolean localLogout) {
        return localLogout ? AsyncLocalLogout.ALWAYS_LOGOUT : AsyncLocalLogout.PROFILE_PRESENCE_DEPENDENT;
    }

    protected AsyncCentralLogoutStrategy getCentralLogoutStrategy(final boolean centralLogout) {
        return centralLogout ? AsyncCentralLogout.CENTRAL_LOGOUT : AsyncCentralLogout.NO_CENTRAL_LOGOUT;
    }
}
