package org.pac4j.async.core.exception.handler;

import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

/**
 * Simple exception unwrapper for CompletionExeeptions to unwrap the underlying exception where more than one
 * CompletionStage may be involved.
 */
public class AsyncExceptionHandler {

    public static void handleException(final Throwable t, final Consumer<Throwable> unwrappedThrowableHandler) {

        final Throwable unwrapped = (t instanceof CompletionException) ? t.getCause() : t;
        unwrappedThrowableHandler.accept(unwrapped);
    }

}
