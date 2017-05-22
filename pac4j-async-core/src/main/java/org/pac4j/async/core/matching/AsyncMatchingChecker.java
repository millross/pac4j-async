package org.pac4j.async.core.matching;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The way to check requests matching - async version.
 *
 */
public interface AsyncMatchingChecker {
    /**
     * Check if the web context matches.
     *
     * @param context the async web context being matched
     * @param matcherNames the async matchers to invoke
     * @param matchersMap the map of matchers which should include a named superset of the matchers to invoke
     * @return CompletableFuture - completes with true if matches, false if not, or completes exceptionally with
     * HttpAction if an additional HTTP action is required
     */
    CompletableFuture<Boolean> matches(AsyncWebContext context, String matcherNames, Map<String, AsyncMatcher> matchersMap);
}

