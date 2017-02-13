package org.pac4j.async.core.context;

import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.WebContextBase;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public interface AsyncWebContext<I> extends WebContextBase<AsyncSessionStore<I, AsyncWebContext<I>>> {

    /**
     * Get the session store.
     *
     * @return the session store
     */
    default AsyncSessionStore<I, AsyncWebContext<I>> getSessionStore() {
        throw new UnsupportedOperationException("To be implemented");
    }

    /**
     * Set the session store.
     *
     * @param sessionStore the session store
     */
    default void setSessionStore(AsyncSessionStore<I, AsyncWebContext<I>> sessionStore) {
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
    default CompletableFuture<I> getSessionIdentifier() {
        return getSessionStore().getOrCreateSessionId(this);
    }

}
