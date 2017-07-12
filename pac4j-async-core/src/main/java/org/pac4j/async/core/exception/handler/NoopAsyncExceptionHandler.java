package org.pac4j.async.core.exception.handler;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 * No-op exception handling, simply complete the future as it would already complete or re-throw any exceptional
 * completion for interpretation by external code.
 */
public class NoopAsyncExceptionHandler<T> implements AsyncExceptionHandler<T> {
    @Override
    public CompletableFuture<T> applyExceptionHandling(CompletableFuture<T> future, AsyncWebContext webContext) {
        return future.handle(ExceptionSoftener.softenBiFunction((v, t) -> {
           if (v != null) {
               return v;
           } else {
               throw t;
           }
        }));
    }
}
