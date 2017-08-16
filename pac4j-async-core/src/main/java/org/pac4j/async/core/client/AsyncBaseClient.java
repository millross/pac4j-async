package org.pac4j.async.core.client;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.authenticate.failure.recorder.RecordFailedAuthenticationStrategy;
import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.credentials.authenticator.AsyncAuthenticator;
import org.pac4j.async.core.credentials.extractor.AsyncCredentialsExtractor;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.CommonBaseClient;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;
import static org.pac4j.async.core.future.FutureUtils.combineFuturesToList;

/**
 *
 */
public abstract class AsyncBaseClient<C extends Credentials, U extends CommonProfile>
        extends CommonBaseClient<C, U, AsyncWebContext, AsyncAuthorizationGenerator<U>>
        implements AsyncClient<C, U>, ConfigurableByClientsObject<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncBaseClient.class);

    private AsyncCredentialsExtractor<C> credentialsExtractor;
    private AsyncAuthenticator<C> authenticator;

    @Override
    public CompletableFuture<Optional<U>> getUserProfileFuture(final C credentials, final AsyncWebContext context) {
        init();

        logger.debug("credentials : {}", credentials);
        if (credentials == null) {
            return null;
        }

        final CompletableFuture<Optional<U>> profileFuture = retrieveUserProfileFuture(credentials, context);

        // We need to end up with a CompletableFuture of Optional<P>

        return profileFuture.thenCompose(profileOption -> {
            final Optional<CompletableFuture<Optional<U>>> optionalCompletableFuture = profileOption.map(p -> {
                // Frustratingly because we want to use the same set of futures twice (to ensure they will have
                // completed before we get the result which we will then pul lout via join() we have to process
                // the stream, collect, then map to the result
                final List<CompletableFuture<Consumer<U>>> profileModifierFutures = authorizationGenerators
                        .stream()
                        .map(g -> g.generate(context, p))
                        .collect(toList());
                return combineFuturesToList(profileModifierFutures).thenApply(l -> {
                            context.getExecutionContext().runOnContext(() -> l.forEach(c -> c.accept(p)));
                            return Optional.of(p);
                        });
            });
            return optionalCompletableFuture.
                    // Unwrap, substituting the empty future if we don't get anything, I think (to review)
                    orElse(CompletableFuture.completedFuture(Optional.empty()));
        });

    }

    /**
     * Retrieve a user userprofile.
     *
     * @param credentials the credentials
     * @param context the web context
     * @return CompletableFuture wrapping the user profile
     * Note that unlike the sync version this won't throw HttpAction as that's a job for the underlying computation,
     * rather than the future wrapper
     */
    protected abstract CompletableFuture<Optional<U>> retrieveUserProfileFuture(final C credentials, final AsyncWebContext context);

    /**
     * Retrieve the strategy for recording failed authentication attempts. This lets us define getCredentials in the same
     * way for all clients, as the fundamental variation is whether or not any recording must be made of failed
     * authentication attempts.
     */
    protected abstract RecordFailedAuthenticationStrategy authFailureRecorder();

    @Override
    public void configureFromClientsObject(Clients<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> toConfigureFrom) {
        if (!toConfigureFrom.getAuthorizationGenerators().isEmpty()) {
            addAuthorizationGenerators(toConfigureFrom.getAuthorizationGenerators());
        }
    }

    public AsyncCredentialsExtractor<C> getCredentialsExtractor() {
        return credentialsExtractor;
    }

    protected void defaultCredentialsExtractor(final AsyncCredentialsExtractor<C> credentialsExtractor) {
        if (this.credentialsExtractor == null) {
            this.credentialsExtractor = credentialsExtractor;
        }
    }

    public void setCredentialsExtractor(final AsyncCredentialsExtractor<C> credentialsExtractor) {
        this.credentialsExtractor = credentialsExtractor;
    }

    /**
     * Retrieve the credentials.
     *
     * @param context the web context
     * @return the credentials
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected CompletableFuture<C> retrieveCredentials(final AsyncWebContext context) throws HttpAction {
        return this.credentialsExtractor.extract(context)
                .thenCompose(creds -> {
                    return Optional.ofNullable(creds)
                            .map(c -> this.authenticator.validate(creds, context).thenApply(v -> creds))
                            .orElse(CompletableFuture.completedFuture(creds)); // The orElse leaves any null returns
                })
                // Now translate a CredentialsException to null
                .handle(ExceptionSoftener.softenBiFunction((creds, throwable) -> {
                    // throwable being non-null means creds will be null, so we can make this check here
                    if (creds != null || throwable instanceof CredentialsException) {
                        return creds;
                    } else {
                        throw throwable;
                    }
                }));
    }

}
