package org.pac4j.async.core.context;

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
     * @return
     */
    AsyncPac4jExecutionContext getExecutionContext();

}
