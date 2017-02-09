package org.pac4j.async.core.authorization.generator;

import io.vertx.core.Context;
import org.pac4j.async.core.execution.context.ContextRunner;

/**
 * Simple context runner for the vert.x framework to show how to construct one.
 */
public class VertxContextRunner implements ContextRunner {

    final Context vertxContext;

    public VertxContextRunner(Context vertxContext) {
        this.vertxContext = vertxContext;
    }

    @Override
    public void runOnContext(Runnable operation) {
        vertxContext.runOnContext(a -> operation.run());
    }
}
