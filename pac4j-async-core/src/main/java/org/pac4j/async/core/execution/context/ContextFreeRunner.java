package org.pac4j.async.core.execution.context;

/**
 * Simple context runner for the situation where we don't care about moving onto a specific context. This should be used
 * with caution as most async frameworks make certain guarantees about thread safety based on running on a particular
 * context (meaning that the state of an actor or similar runnnng on a particular context will only be accessed by
 * a specific thread, ensuring thread safety for thst atate. Vert.x is an example of such an async framework.
 */
public class ContextFreeRunner implements ContextRunner {
    @Override
    public void runOnContext(Runnable operation) {
        operation.run();
    }
}
