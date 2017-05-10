package org.pac4j.async.core;

import io.vertx.core.Context;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;

/**
 * Simple context runner for the vert.x framework to show how to construct one.
 */
public class VertxContextRunner implements AsyncPac4jExecutionContext {

    final Context vertxContext;

    public VertxContextRunner(Context vertxContext) {
        this.vertxContext = vertxContext;
    }

    @Override
    public void runOnContext(Runnable operation) {
        vertxContext.runOnContext(a -> operation.run());
    }
}
