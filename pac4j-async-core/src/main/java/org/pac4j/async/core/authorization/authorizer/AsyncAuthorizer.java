package org.pac4j.async.core.authorization.authorizer;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.AuthorizerBase;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of a pac4j authorizer - we need to convert sync versions into the async version for use. This is
 * merely a simple type definition to keep everything on site.
 *
 */
public interface AsyncAuthorizer<U extends CommonProfile> extends AuthorizerBase <AsyncWebContext<?>,
        CompletableFuture<Boolean>, U> {
}
