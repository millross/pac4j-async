package org.pac4j.async.core.logic;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.authorization.checker.AsyncAuthorizationChecker;
import org.pac4j.async.core.authorization.checker.DefaultAsyncAuthorizationChecker;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.exception.handler.AsyncExceptionHandler;
import org.pac4j.async.core.exception.handler.DefaultAsyncExceptionHandler;
import org.pac4j.async.core.logic.authenticator.AsyncDirectAuthFailedAuthenticator;
import org.pac4j.async.core.logic.authenticator.AsyncDirectClientAuthenticator;
import org.pac4j.async.core.logic.decision.AsyncLoadProfileFromSessionDecision;
import org.pac4j.async.core.logic.decision.AsyncSaveProfileToSessionDecision;
import org.pac4j.async.core.matching.AsyncMatchingChecker;
import org.pac4j.async.core.matching.DefaultAsyncMatchingChecker;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.profile.save.AsyncProfileSaveStrategy;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.finder.ClientFinder;
import org.pac4j.core.client.finder.DefaultClientFinder;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManagerFactoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.aol.cyclops.invokedynamic.ExceptionSoftener.softenFunction;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.pac4j.async.core.profile.save.AsyncProfileSave.MULTI_PROFILE_SAVE;
import static org.pac4j.async.core.profile.save.AsyncProfileSave.SINGLE_PROFILE_SAVE;
import static org.pac4j.core.util.CommonHelper.*;

/**
 *
 */
public class DefaultAsyncSecurityLogic<R, U extends CommonProfile, C extends AsyncWebContext>
        extends ProfileManagerFactoryAware<C, AsyncProfileManager<U, C>, AsyncConfig<R, U, C>> implements AsyncSecurityLogic<R, C> {

    protected static final Logger logger = LoggerFactory.getLogger(DefaultAsyncSecurityLogic.class);

    private ClientFinder<AsyncClient<?, ?>> clientFinder = new DefaultClientFinder<>();

    private AsyncAuthorizationChecker authorizationChecker = new DefaultAsyncAuthorizationChecker();

    private AsyncMatchingChecker matchingChecker = new DefaultAsyncMatchingChecker();

    private final AsyncProfileSaveStrategy saveStrategy;
    private final AsyncConfig<R, U, C> config;
    private final Clients configClients;
    private final HttpActionAdapter<R, C> httpActionAdapter;
    private final AsyncLoadProfileFromSessionDecision<C> loadFromSessionDecision = new AsyncLoadProfileFromSessionDecision<>();
    private final AsyncDirectClientAuthenticator directClientAuthenticator; // to attempt direct client authentication initially
    private final AsyncDirectAuthFailedAuthenticator<C> directAuthFailedAuthenticator = new AsyncDirectAuthFailedAuthenticator<>();

    // Can be injected to provide custom exception handling for different frameworks, defaults to throwing the exception out
    // to the execution context provided.
    private final AsyncExceptionHandler<R> exceptionHandler;

    public DefaultAsyncSecurityLogic(final boolean saveProfileInSession,
                                     final boolean multiProfile,
                                     final AsyncConfig<R, U, C> config,
                                     final HttpActionAdapter<R, C> httpActionAdapter) throws Exception {
        this(saveProfileInSession, multiProfile, config, new DefaultAsyncExceptionHandler<>(), httpActionAdapter);
    }

    public DefaultAsyncSecurityLogic(final boolean saveProfileInSession,
                                     final boolean multiProfile,
                                     final AsyncConfig<R, U, C> config,
                                     final AsyncExceptionHandler<R> exceptionHandler,
                                     final HttpActionAdapter<R, C> httpActionAdapter) throws Exception {

        assertNotNull("config", config);
        assertNotNull("httpActionAdapter", httpActionAdapter);
        this.configClients = config.getClients();
        assertNotNull("configClients", config.getClients());

        this.saveStrategy = multiProfile ? MULTI_PROFILE_SAVE : SINGLE_PROFILE_SAVE;
        this.config = config;
        this.httpActionAdapter = httpActionAdapter;
        // Exception handler, to be used to deal with exceptional completion of futures
        this.exceptionHandler = exceptionHandler;
        directClientAuthenticator = new AsyncDirectClientAuthenticator(saveStrategy, new AsyncSaveProfileToSessionDecision<U, C>(saveProfileInSession),
                this.loadFromSessionDecision);
    }

    @Override
    public CompletableFuture<R> perform(final C context,
                                        final SecurityGrantedAccessAdapter<R, C> securityGrantedAccessAdapter,
                                        final String clients, final String authorizers, final String matchers,
                                        final Object... parameters) {

        logger.debug("=== SECURITY ===");

        // checks
        assertNotNull("context", context);

        // logic
        HttpAction action;
        logger.debug("url: {}", context.getFullRequestURL());
        logger.debug("matchers: {}", matchers);

        final CompletableFuture<R> resultFuture = matchingChecker.matches(context, matchers, config.getMatchers())
                .thenCompose(b -> {
                    if (b) {

                        logger.debug("clients: {}", clients);
                        final List<AsyncClient<? extends Credentials, U>> currentClients = clientFinder.find(configClients, context, clients);
                        logger.debug("currentClients: {}", currentClients);
                        final boolean loadProfilesFromSession = loadFromSessionDecision.make(context, currentClients);
                        logger.debug("loadProfilesFromSession: {}", loadProfilesFromSession);
                        final AsyncProfileManager<U, C> manager = getProfileManager(context, config);
                        final CompletableFuture<List<U>> profilesFuture = manager.getAll(loadProfilesFromSession);
                        final CompletableFuture<List<U>> profilesAfterDirectAuth = profilesFuture.thenCompose(profiles -> {
                            logger.debug("profiles: {}", profiles);

                            // no profile and some current clients
                            if (isEmpty(profiles) && isNotEmpty(currentClients)) {
                                return directClientAuthenticator.authenticate(currentClients,
                                        context,
                                        manager);
                            } else {
                                return completedFuture(profiles);
                            }
                        });

                        // Transform to http action to be adapted
                        return profilesAfterDirectAuth.thenCompose(profiles -> {
                            if (isNotEmpty(profiles)) {
                                logger.debug("authorizers: {}", authorizers);
                                final CompletableFuture<Boolean> authorizedFuture =
                                        authorizationChecker.isAuthorized(context, profiles, authorizers, config.getAuthorizers());
                                return authorizedFuture.thenCompose(authorized ->
                                        handleAuthorizationResult(authorized, securityGrantedAccessAdapter, context, parameters));
                            } else {
                                return directAuthFailedAuthenticator.authenticate(context, currentClients)
                                        .handle(ExceptionSoftener.softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction))
                                        .handle(AsyncExceptionHandler::wrapUnexpectedException)
                                        .thenApply(a -> httpActionAdapter.adapt(a.getCode(), context));
                            }
                        });
                    } else {
                        // No matching, act accordinglylogger.debug("no matching for this request -> grant access");
                        return accessApproved(securityGrantedAccessAdapter, context, parameters);
                    }
                });

        // Apply exception handling to the result. If there is a desire to customize exception handling, either a different handler
        // can be injected or a noop handler can be used here, meaning that an external handler can be applied to this future instead.
        return exceptionHandler.applyExceptionHandling(resultFuture, context);
    }



    protected CompletableFuture<R> handleAuthorizationResult(final boolean authorized,
                                                             final SecurityGrantedAccessAdapter securityGrantedAccessAdapter,
                                                             final C context,
                                                             final Object... parameters) {
        if (authorized) {
            logger.debug("authenticated and authorized -> grant access");
            return accessApproved(securityGrantedAccessAdapter, context, parameters);
        } else {
            throw forbidden(context);
        }
    }

    protected CompletableFuture<R> accessApproved(final SecurityGrantedAccessAdapter<R, C> securityGrantedAccessAdapter,
                                                  final C context, final Object... parameters) {
        return CompletableFuture.<Void>completedFuture(null)
                .thenApply(softenFunction(t -> securityGrantedAccessAdapter.adapt(context, parameters)))
                .handle(AsyncExceptionHandler::wrapUnexpectedException);
    }

    /**
     * Return a forbidden error.
     *
     * @param context the web context
     * @return a forbidden error
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected HttpAction forbidden(final C context) throws HttpAction {
        return HttpAction.forbidden("forbidden", context);
    }

    @Override
    protected Function<C, AsyncProfileManager<U, C>> defaultProfileManagerFactory() {
        return ctx -> new AsyncProfileManager(ctx);
    }
}
