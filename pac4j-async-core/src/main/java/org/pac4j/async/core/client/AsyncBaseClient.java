package org.pac4j.async.core.client;

import org.pac4j.async.core.authorization.generator.AsyncAuthorizationGenerator;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.definition.ProfileDefinitionAware;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public abstract class AsyncBaseClient<C extends Credentials, U extends CommonProfile> extends ProfileDefinitionAware<U>
        implements AsyncClient<C, U>, ConfigurableByClientsObject<AsyncClient<C, U>, AsyncAuthorizationGenerator<U>> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    private List<AsyncAuthorizationGenerator<U>> authorizationGenerators = new ArrayList<>();

//    @Override = make this async
//    public final U getUserProfile(final C credentials, final WebContext context) throws HttpAction {
//        init(context);
//        logger.debug("credentials : {}", credentials);
//        if (credentials == null) {
//            return null;
//        }
//
//        final U profile = retrieveUserProfile(credentials, context);
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
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected abstract CompletableFuture<U> retrieveUserProfile(final C credentials, final WebContext context) throws HttpAction;

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
