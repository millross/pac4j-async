package org.pac4j.async.core.context;

/**
 * Interface to be implemented for running tasks on a framework's context. Most async frameworks have the concept of
 * a context on which callback code runs, future completion tasks run, etc. There needs to be a way of ensuring that
 * some code in pac4j runs on a particular context, particularly for frameworks like vert.x where some code is
 * guaranteed to be bound to a specific thread, which leads to thread safety for access on certain objects. An instance
 * of an object implementing this interface must be provided where there is code which might need to run on such a
 * context.
 */
public interface ContextRunner {

    void runOnContext(Runnable operation);

}
