package org.pac4j.async.core.authorization.authorizer.csrf;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 * CSRF token generator - async version.
 *
 * @author Jeremy Prime
 * @since 2.0.0-SNAPSHOT
 */
public interface AsyncCsrfTokenGenerator {
    /**
     * Get the CSRF token from the session or create it if it doesn't exist.
     *
     * @param context the current web context
     * @return the CSRF token
     */
    CompletableFuture<String> get(AsyncWebContext context);
}
