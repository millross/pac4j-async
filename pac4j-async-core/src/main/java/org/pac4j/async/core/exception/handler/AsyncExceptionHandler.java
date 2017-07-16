package org.pac4j.async.core.exception.handler;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Simple exception handling for CompletionExeeptions to unwrap the underlying exception where more than one
 * CompletionStage may be involved.
 */
public interface AsyncExceptionHandler<T> {

    static void handleException(final Throwable t, final Consumer<Throwable> unwrappedThrowableHandler) {
        unwrappedThrowableHandler.accept(unwrapAsyncException(t));
    }

    static HttpAction extractAsyncHttpAction(final HttpAction action, final Throwable t) throws Throwable {

        final Throwable unwrapped = unwrapAsyncException(t);
        final HttpAction result = action != null ? action : (unwrapped instanceof HttpAction ? (HttpAction) unwrapped : null);
        if (result != null) {
            return result;
        } else throw unwrapped;
    }

    static <T> T wrapUnexpectedException(final T result, final Throwable t) {
        if (result != null) {
            return result;
        } else {
            // Http actions should be thrown outwards as is
            if (t instanceof HttpAction) {
                throw (HttpAction) t;
            } else {
                // All other exceptions should be wrapped in TechnicalExceptions, if not already wrapped
                final TechnicalException wrapped = (t instanceof TechnicalException) ? (TechnicalException) t : new TechnicalException(t);
                throw wrapped;
            }
        }
    }


    static Throwable unwrapAsyncException(final Throwable t) {
        return (t instanceof CompletionException) ? t.getCause() : t;
    }

    /**
     * Interface to be implemented to offer custom exception handling to completable futures returned by
     * logic (either security, callback or logout logic).
     *
     * A default implementation will be provided which offers use of a webContext's execution context. In addition
     * a "do nothing" implementation will be offered which permits external implementations to handle exceptions
     * in their own code rather than via injection of a handler into the relevant logic.
     * @param future
     * @return
     */
    CompletableFuture<T> applyExceptionHandling(final CompletableFuture<T> future, final AsyncWebContext webContext);

}
