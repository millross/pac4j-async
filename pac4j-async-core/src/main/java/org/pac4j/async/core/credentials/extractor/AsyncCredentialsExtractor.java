package org.pac4j.async.core.credentials.extractor;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.AsynchronousComputation;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous credentials extractor to be used by async clients.
 */
public interface AsyncCredentialsExtractor<C extends Credentials> {

    /**
     * Given an existing non-blocking but synchronous CredentialsExtractor, convert it into an async non-blocking
     * one
     */
    static <C extends Credentials> AsyncCredentialsExtractor<C> fromNonBlockingExtractor(final CredentialsExtractor<C, WebContextBase<?>> syncExtractor) {
        return context -> AsynchronousComputation.fromNonBlocking(ExceptionSoftener.softenSupplier(() -> syncExtractor.extract(context)));
    }
    /**
     * Extract the right credentials, using asynchronouse methods.
     *
     * It should return a CompletableFuture which will either complete with the credentials
     * or complete exceptionally with an HttpAction if a specific HTTP action is required
     * or complete exceptionally  with a CredentialsException if the credentials are invalid

     * @param context the current web context
     * @return the credentials
     */
    CompletableFuture<C> extract(AsyncWebContext context);

}
