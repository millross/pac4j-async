package org.pac4j.async.core.client;

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.CommonBaseClient;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 *
 */
public abstract class AsyncBaseClient<C extends Credentials, U extends CommonProfile>
        extends CommonBaseClient<C, U, AsyncWebContext, AsyncAuthorizationGenerator<U>>
        implements AsyncClient<C, U>, ConfigurableByClientsObject<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncBaseClient.class);

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
                final CompletableFuture<Consumer<U>>[] profileModifiersFutureArray = authorizationGenerators.stream()
                        .map(g -> g.generate(context, p))
                        .toArray(CompletableFuture[]::new);
                return CompletableFuture.allOf((CompletableFuture<?>[]) profileModifiersFutureArray)
                        .thenApply(v -> {
                            logger.debug("All profile modifiers determined " + System.currentTimeMillis());
                            return v;
                        })
                        // Convert to a list of consumers
                        .thenApply(v -> {
                            final List<Consumer<U>> profileModifiers = Arrays.asList(profileModifiersFutureArray)
                                    .stream()
                                    .map(f -> f.join())
                                    .collect(toList());
                            return profileModifiers;
                        }).thenApply(l -> {
                            // And then apply every consumer in that list to the profile - we know that to get here we've
                            // already completed the profile futrue so this is clean. When this future completes, all
                            // modifiers have been applied to the profile. note that we ensure we run on the context
                            // with the intent that we will then be respecting threading guarantees made by the framrwork
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

    @Override
    public void configureFromClientsObject(Clients<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> toConfigureFrom) {
        if (!toConfigureFrom.getAuthorizationGenerators().isEmpty()) {
            addAuthorizationGenerators(toConfigureFrom.getAuthorizationGenerators());
        }
    }

}
