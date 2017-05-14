package org.pac4j.async.core.matching;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of the Matcher class
 */
public interface AsyncMatcher {

    /**
     * Check if the web context matches.
     *
     * @param context the web context
     * @return CompletableFuture which will resolve to true in the event of a match, false in no match
     * or completeExceptionally in the case of an exception during determination of matching. This exception
     * can be an HttpAction so can lead to further http behaviour
     *
     */
    CompletableFuture<Boolean> matches(AsyncWebContext context);


}
