package org.pac4j.async.core.context;

import org.pac4j.async.core.AsynchronousComputation;
import org.pac4j.async.core.execution.context.AsyncPac4jExecutionContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.WebContextBase;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface AsyncWebContext extends WebContextBase<AsyncSessionStore> {

    /**
     * Get the session store.
     *
     * @return the session store
     */
    default AsyncSessionStore getSessionStore() {
        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Set the session store.
     *
     * @param sessionStore the session store
     */
    default void setSessionStore(AsyncSessionStore sessionStore) {
        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Save an attribute in session.
     *
     * @param name name of the session attribute
     * @param value value of the session attribute
     */
    default <T> CompletableFuture<Void> setSessionAttribute(String name, T value) {
        return getSessionStore().set(this, name, value);
    }

    /**
     * Get an attribute from session.
     *
     * @param name name of the session attribute
     * @return the session attribute
     */
    default <T> CompletableFuture<T> getSessionAttribute(String name) {
        return getSessionStore().get(this, name);
    }

    /**
     * Gets the session id for this context.
     *
     * @return the session identifier
     */
    default CompletableFuture<String> getSessionIdentifier() {
        return getSessionStore().getOrCreateSessionId(this);
    }

    /**
     * Get the execution context which was in force when this web context was created. This is so that for context-aware
     * frameworks such as vert.x we will always access this web context from the right context (fulfilling threading
     * guarantees made by the framework). Note that there is a ContextFreePac4jExecutionContext where the implementing
     * framework does not relate threads to contexts.
     *
     * This method should generally not be overridden because we want the async adapter and the async web context to use
     * the same underlying framework context (to respect threading, exception handling etc guarantees)
     * @return
     */
    default AsyncPac4jExecutionContext getExecutionContext() {
        return getAsyncComputationAdapter().getExecutionContext();
    }

    /**
     * Get the asynchronous computation adapter (which is also based on current execution context) for the AsyncWebContext
     * This is primarily used for converting blocking sync code into code which will run correctly in an async manner
     * on the context.
     */
    AsynchronousComputation getAsyncComputationAdapter();
}
