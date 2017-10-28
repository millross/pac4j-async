package org.pac4j.async.vertx.execution.context;

import io.vertx.core.Context;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

/**
 * Async execution context for vert.x implementations. This enables us to run particular operations on the correct
 * Vert.x context
 */
public class VertxAsyncPac4jExecutionContext implements AsyncPac4jExecutionContext {

    final Context vertxContext;

    public VertxAsyncPac4jExecutionContext(Context vertxContext) {
        this.vertxContext = vertxContext;
    }

    @Override
    public void runOnContext(Runnable operation) {
        vertxContext.runOnContext(a -> operation.run());
    }
}

