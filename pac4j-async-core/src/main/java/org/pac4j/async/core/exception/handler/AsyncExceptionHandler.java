package org.pac4j.async.core.exception.handler;

import org.pac4j.core.exception.HttpAction;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Simple exception unwrapper for CompletionExeeptions to unwrap the underlying exception where more than one
 * CompletionStage may be involved.
 */
public class AsyncExceptionHandler {

    public static void handleException(final Throwable t, final Consumer<Throwable> unwrappedThrowableHandler) {
        unwrappedThrowableHandler.accept(unwrapAsyncException(t));
    }

    public static HttpAction extractAsyncHttpAction(final HttpAction action, final Throwable t) throws Throwable {

        final Throwable unwrapped = unwrapAsyncException(t);
        final HttpAction result = action != null ? action : (unwrapped instanceof HttpAction ? (HttpAction) unwrapped : null);
        if (result != null) {
            return result;
        } else throw unwrapped;
    }

    private static Throwable unwrapAsyncException(final Throwable t) {
        return (t instanceof CompletionException) ? t.getCause() : t;

    }

}
