package org.pac4j.async.core.client;

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.async.core.context.ContextRunner;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.ProfileDefinitionAware;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 *
 */
public abstract class AsyncBaseClient<C extends Credentials, U extends CommonProfile> extends ProfileDefinitionAware<U>
        implements AsyncClient<C, U>, ConfigurableByClientsObject<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> {

    protected static final Logger logger = LoggerFactory.getLogger(AsyncBaseClient.class);

    private final ContextRunner contextRunner;
    private String name;

    private List<AsyncAuthorizationGenerator<U>> authorizationGenerators = new ArrayList<>();

    public AsyncBaseClient(ContextRunner contextRunner) {
        this.contextRunner = contextRunner;
    }

    @Override
    public CompletableFuture<U> getUserProfileFuture(final C credentials, final WebContext context) {
        init(context);

        logger.debug("credentials : {}", credentials);
        if (credentials == null) {
            return null;
        }

        final CompletableFuture<U> profileFuture = retrieveUserProfileFuture(credentials, context);

        profileFuture.thenCompose(p -> {
            // get a list of futures which wrap the auth modifiers created by applying the generators to the profile
            final CompletableFuture<Consumer<U>>[] profileModifiersFutureArray = authorizationGenerators.stream()
                    .map(g -> g.generate(p))
                    .toArray(CompletableFuture[]::new);
            // Take this list of futures, and when all complete
            return CompletableFuture.allOf((CompletableFuture<?>[]) profileModifiersFutureArray)
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
                        contextRunner.runOnContext(() -> l.forEach(c -> c.accept(p)));
                        return p;
                    });
        });

        return profileFuture;

    }

    //    @Override = make this async
//    public final U getUserProfileFuture(final C credentials, final WebContext context) throws HttpAction {
//        init(context);
//        logger.debug("credentials : {}", credentials);
//        if (credentials == null) {
//            return null;
//        }
//
//        final U profile = retrieveUserProfileFuture(credentials, context);
//        if (profile != null) {
//            profile.setClientName(getName());
//            if (this.authorizationGenerators != null) {
//                for (AsyncAuthorizationGenerator<U> authorizationGenerator : this.authorizationGenerators) {
//                    authorizationGenerator.generate(profile);
//                }
//            }
//        }
//        return profile;
//    }

    /**
     * Retrieve a user userprofile.
     *
     * @param credentials the credentials
     * @param context the web context
     * @return CompletableFuture wrapping the user profile
     * Note that unlike the sync version this won't throw HttpAction as that's a job for the underlying computation,
     * rather than the future wrapper
     */
    protected abstract CompletableFuture<U> retrieveUserProfileFuture(final C credentials, final WebContext context);

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        if (CommonHelper.isBlank(this.name)) {
            return this.getClass().getSimpleName();
        }
        return this.name;
    }

    @Override
    public void configureFromClientsObject(Clients<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> toConfigureFrom) {
        if (!toConfigureFrom.getAuthorizationGenerators().isEmpty()) {
            addAuthorizationGenerators(toConfigureFrom.getAuthorizationGenerators());
        }
    }

    /**
     * Notify of the web session renewal.
     *
     * @param oldSessionId the old session identifier
     * @param context the web context
     */
    public void notifySessionRenewal(final String oldSessionId, final WebContext context) { }

    public List<AsyncAuthorizationGenerator<U>> getAuthorizationGenerators() {
        return this.authorizationGenerators;
    }

    public void setAuthorizationGenerators(final List<AsyncAuthorizationGenerator<U>> authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = authorizationGenerators;
    }

    public void setAuthorizationGenerators(final AsyncAuthorizationGenerator<U>... authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = Arrays.asList(authorizationGenerators);
    }

    /**
     * Add an authorization generator.
     *
     * @param authorizationGenerator an authorizations generator
     */
    public void setAuthorizationGenerator(final AsyncAuthorizationGenerator<U> authorizationGenerator) {
        addAuthorizationGenerator(authorizationGenerator);
    }

    public void addAuthorizationGenerator(final AsyncAuthorizationGenerator<U> authorizationGenerator) {
        CommonHelper.assertNotNull("authorizationGenerator", authorizationGenerator);
        this.authorizationGenerators.add(authorizationGenerator);
    }

    public void addAuthorizationGenerators(final List<AsyncAuthorizationGenerator<U>> authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators.addAll(authorizationGenerators);
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "name", getName(),
                "authorizationGenerators", authorizationGenerators);
    }

}
