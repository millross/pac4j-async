package org.pac4j.core.client;

import org.pac4j.async.core.profile.definition.ProfileDefinitionAware;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common base client for both async and sync clients, the sync/async base clients should inherit from
 * Type parameters are as follows:-
 * C = credentials implementor
 * U = profile implementor
 * WC = web context implementor (typically WebContext or AsyncWebContext)
 * AG = authorization generator implementor (typically AuthorizationGenerator or AsyncAuthorizationGenerator)
 */
public abstract class CommonBaseClient<C extends Credentials, U extends CommonProfile, WC extends WebContext<?>, AG>
        extends ProfileDefinitionAware <U>{

    private static final Logger logger = LoggerFactory.getLogger(CommonBaseClient.class);

    private String name;
    protected List<AG> authorizationGenerators = new ArrayList<>();

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        if (CommonHelper.isBlank(this.name)) {
            return this.getClass().getSimpleName();
        }
        return this.name;
    }

    /**
     * Notify of the web session renewal.
     *
     * @param oldSessionId the old session identifier
     * @param context the web context
     */
    public void notifySessionRenewal(final String oldSessionId, final WC context) { }

    public List<AG> getAuthorizationGenerators() {
        return this.authorizationGenerators;
    }

    public void setAuthorizationGenerators(final List<AG> authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = authorizationGenerators;
    }

    public void setAuthorizationGenerators(final AG... authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators = Arrays.asList(authorizationGenerators);
    }

    /**
     * Add an authorization generator.
     *
     * @param authorizationGenerator an authorizations generator
     */
    public void setAuthorizationGenerator(final AG authorizationGenerator) {
        addAuthorizationGenerator(authorizationGenerator);
    }

    public void addAuthorizationGenerator(final AG authorizationGenerator) {
        CommonHelper.assertNotNull("authorizationGenerator", authorizationGenerator);
        this.authorizationGenerators.add(authorizationGenerator);
    }

    public void addAuthorizationGenerators(final List<AG> authorizationGenerators) {
        CommonHelper.assertNotNull("authorizationGenerators", authorizationGenerators);
        this.authorizationGenerators.addAll(authorizationGenerators);
    }

    @Override
    public String toString() {
        return CommonHelper.toString(this.getClass(), "name", getName(),
                "authorizationGenerators", authorizationGenerators);
    }

}
