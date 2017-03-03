package org.pac4j.async.core.authorization.checker;

import org.pac4j.async.core.authorization.authorizer.AsyncAuthorizer;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of the pac4j synchronous authorization checker
 */
public interface AsyncAuthorizationChecker {
    /**
     * Check whether the user is authorized.
     *
     * @param context the web context
     * @param profiles the profile
     * @param authorizerNames the authorizers
     * @param authorizersMap the map of authorizers
     * @return whether the user is authorized.
     * @throws HttpAction whether an additional HTTP action is required
     */
    CompletableFuture<Boolean> isAuthorized(AsyncWebContext context, List<CommonProfile> profiles, String authorizerNames, Map<String, AsyncAuthorizer> authorizersMap) throws HttpAction;
}
