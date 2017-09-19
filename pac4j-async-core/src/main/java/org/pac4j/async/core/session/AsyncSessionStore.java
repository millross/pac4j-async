package org.pac4j.async.core.session;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.context.WebContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous version of the SessionStore interface to allow for the fact there may be blocking i/o on a session
 * store, meaning that it should be treated asynchronously
 */
public interface AsyncSessionStore {

    /**
     * Get or create the session identifier and initialize the session with it if necessary.
     *
     * @param context the web context
     * @return the session identifier
     */
    CompletableFuture<String> getOrCreateSessionId(AsyncWebContext context);

    /**
     * Get the object from its key in store.
     *
     * @param context the web context
     * @param key the key of the object
     * @return the object in store
     */
    <T> CompletableFuture<T> get(WebContext<AsyncSessionStore> context, String key);

    /**
     * Save an object in the store by its key.
     *
     * @param context the web context
     * @param key the key of the object
     * @param value the value to save in store
     */
    <T> CompletableFuture<Void> set(WebContext<AsyncSessionStore> context, String key, T value);

    /**
     * Destroy the web session.
     *
     * @param context the web context
     * @return whether the session has been destroyed
     */
    default CompletableFuture<Boolean> destroySession(AsyncWebContext context) {
        // by default, the session has not been destroyed and this can be done immediately
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Get the native session as a trackable object.
     *
     * @param context the web context
     * @return the trackable object
     */
    default CompletableFuture<Optional<Object>> getTrackableSession(AsyncWebContext context) {
        // by default, the session store does not know how to keep track the native session
        return CompletableFuture.completedFuture(Optional.empty());
    }

    /**
     * Build a new session store from a trackable session.
     *
     * @param context the web context
     * @param trackableSession the trackable session
     * @return the new session store
     */
    default Optional<AsyncSessionStore> buildFromTrackableSession(AsyncWebContext context, Object trackableSession) {
        // by default, the session store does not know how to build a new session store
        return Optional.empty();
    }

    /**
     * Renew the native session by copying all data to a new one.
     *
     * @param context the web context
     * @return whether the session store has renewed the session
     */
    default CompletableFuture<Boolean> renewSession(AsyncWebContext context) {
        // by default, the session store does not know how to renew the native session
        return CompletableFuture.completedFuture(false);
    }

}
