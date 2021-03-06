package org.pac4j.async.core.matching;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.AsynchronousComputationAdapter;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.matching.Matcher;

import java.util.concurrent.CompletableFuture;

/**
 * Async version of the Matcher class
 */
public interface AsyncMatcher {

    /**
     * Given an existing non-blocking but synchronous Matcher, convert it into an async non-blocking
     * one
     */
    static AsyncMatcher fromNonBlocking(final Matcher<WebContext<?>> syncMatcher) {
        return context -> AsynchronousComputationAdapter.fromNonBlocking(ExceptionSoftener.softenSupplier(() -> syncMatcher.matches(context)));
    }

    /**
     * Given an existing blocking and synchronous Matcher, convert it into an async matcher
     * one
     */
    static AsyncMatcher fromBlocking(final Matcher<WebContext<?>> syncMatcher) {
        return context -> context.getAsyncComputationAdapter().fromBlocking(ExceptionSoftener.softenSupplier(() -> syncMatcher.matches(context)));
    }

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
