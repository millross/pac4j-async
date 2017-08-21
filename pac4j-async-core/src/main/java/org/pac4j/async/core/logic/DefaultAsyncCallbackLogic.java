package org.pac4j.async.core.logic;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.authenticate.AsyncClientAuthenticator;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.exception.handler.AsyncExceptionHandler;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.profile.save.AsyncProfileSaveStrategy;
import org.pac4j.async.core.session.renewal.AsyncSessionRenewal;
import org.pac4j.async.core.session.renewal.AsyncSessionRenewalStrategy;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.HttpActionAdapter;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManagerFactoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.pac4j.async.core.profile.save.AsyncProfileSave.MULTI_PROFILE_SAVE;
import static org.pac4j.async.core.profile.save.AsyncProfileSave.SINGLE_PROFILE_SAVE;
import static org.pac4j.async.core.session.renewal.AsyncSessionRenewal.NEVER_RENEW;
import static org.pac4j.core.util.CommonHelper.*;

/**
 *
 */
public class DefaultAsyncCallbackLogic<R, U extends CommonProfile, WC extends AsyncWebContext>
        extends ProfileManagerFactoryAware<WC, AsyncProfileManager<U, WC>, AsyncConfig<R, U, WC>> implements AsyncCallbackLogic<R, U, WC> {

    private static final  Logger logger = LoggerFactory.getLogger(DefaultAsyncCallbackLogic.class);

    protected final AsyncProfileSaveStrategy saveStrategy;
    protected final AsyncSessionRenewalStrategy sessionRenewalStrategy;
    protected final AsyncIndirectAuthenticationFlow<WC> indirectAuthenticationFlow;
    protected final AsyncClientAuthenticator<U, WC> perClientAuthenticator = new AsyncClientAuthenticator<>();
    protected final AsyncConfig<R, U, WC> config;
    protected final HttpActionAdapter<R, WC> httpActionAdapter;

    public DefaultAsyncCallbackLogic(final boolean multiProfile,
                                     final boolean renewSession,
                                     final AsyncConfig<R, U, WC> config,
                                     final HttpActionAdapter<R, WC> httpActionAdapter) {
        assertNotNull("config", config);
        assertNotNull("httpActionAdapter", httpActionAdapter);

        // it doesn't make sense to mix and match single and multi profile saving for an instance of the logic
        this.saveStrategy = multiProfile ? MULTI_PROFILE_SAVE : SINGLE_PROFILE_SAVE;
        this.sessionRenewalStrategy = renewSession ? AsyncSessionRenewal.ALWAYS_RENEW : NEVER_RENEW;
        this.config = config;
        this.httpActionAdapter = httpActionAdapter;
        this.indirectAuthenticationFlow = new AsyncIndirectAuthenticationFlow<>();
    }

    public CompletableFuture<R> perform(final WC context,
                                        final AsyncConfig<R, U, WC> config,
                                        final HttpActionAdapter<R, WC> httpActionAdapter,
                                        final String inputDefaultUrl) {

        logger.debug("=== CALLBACK ===");
        final String defaultUrl = Optional.ofNullable(inputDefaultUrl).orElse(Pac4jConstants.DEFAULT_URL_VALUE);

        // checks
        assertNotNull("context", context);
        assertNotBlank(Pac4jConstants.DEFAULT_URL, defaultUrl);
        final Clients<AsyncClient<? extends Credentials, ? extends U>, AsyncAuthorizationGenerator<U>> clients = config.getClients();
        assertNotNull("clients", clients);

        // logic
        final AsyncClient<? extends Credentials, ? extends U> client = clients.findClient(context);
        logger.debug("client: {}", client);
        assertNotNull("client", client);
        assertTrue(client.isIndirect(), "only indirect clients are allowed on the callback url");

        // Attempt authentication, retrieving profile if available
        final CompletableFuture<Void> sessionRenewalFuture = perClientAuthenticator.authenticateFor(client, context)
                // Apply save strategy to the profile (absent profile will lead to completion false and no save)
                .thenCompose(profileOption -> saveStrategy.saveProfile(getProfileManager(context, config),
                        p -> true, profileOption.orElse(null)))
                // If we saved, renew the session, and attempt redirect
                .thenCompose(b -> {
                    if (b) {
                        // We have authenticated and saved so renew the session if supported
                        return sessionRenewalStrategy.renewSession(context, config);
                    } else {
                        // Authentication failed -> expand this
                        return CompletableFuture.completedFuture(null); // No need to renew session if we haven't written to it
                    }
                });
        // Generate redirect action for originally requested url. At time of writing this matches previous behaviour
        final CompletableFuture<HttpAction> actionFuture = sessionRenewalFuture.thenCompose(v ->
                indirectAuthenticationFlow.redirectToOriginallyRequestedUrl(context, defaultUrl));

        // Apply standard exception handling including http actions thrown during processing
        return actionFuture.handle(ExceptionSoftener.softenBiFunction(AsyncExceptionHandler::extractAsyncHttpAction))
                .handle(AsyncExceptionHandler::wrapUnexpectedException)
                .thenApply(a -> httpActionAdapter.adapt(a.getCode(), context));

        }

    @Override
    protected Function<WC, AsyncProfileManager<U, WC>> defaultProfileManagerFactory() {
        return AsyncProfileManager::new;
    }

    private <C extends Credentials, P extends U>CompletableFuture<Boolean> authResultFuture(AsyncClient<C, P> client, WC context,
                                                                                                  AsyncConfig<R, U, WC> config) {
        return client.getCredentials(context)
                .thenCompose(c -> {
                    logger.debug("credentials: {}", c);
                    return client.getUserProfileFuture(c, context);
                })
                .thenCompose(profileOption -> profileOption.map(profile -> {
                    logger.debug("profile: {}", profile);
                    return saveStrategy.saveProfile(getProfileManager(context, config), u -> true, profile);
                }).orElse(CompletableFuture.completedFuture(false)));
    }

}
