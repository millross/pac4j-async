package org.pac4j.async.core.exception.handler;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class DefaultAsyncExceptionHandler<T> implements AsyncExceptionHandler<T> {
    @Override
    public CompletableFuture<T> applyExceptionHandling(CompletableFuture<T> future, AsyncWebContext webContext) {
        return future.handle((v, t) -> {
            if (t != null) {
                webContext.getExecutionContext().runOnContext(ExceptionSoftener.softenRunnable(() -> {
                    throw t;
                }));
                return null;
            } else {
                return v;
            }
        });
    }
}
