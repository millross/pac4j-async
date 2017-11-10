package org.pac4j.async.vertx.core.session;

import io.vertx.ext.web.Session;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.pac4j.core.context.WebContext;

import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class VertxAsyncSessionStore implements AsyncSessionStore {

    /**
     * Wrapper AsyncSessionStore wrapper for a vert.x session
     */
    private final Session session;

    public VertxAsyncSessionStore(final Session session) {
        this.session = session;
    }

    @Override
    public CompletableFuture<String> getOrCreateSessionId(AsyncWebContext context) {
        return CompletableFuture.completedFuture(session.id());
    }

    @Override
    public <T> CompletableFuture<T> get(WebContext<AsyncSessionStore> context, String key) {
        return CompletableFuture.completedFuture(session.get(key));
    }

    @Override
    public <T> CompletableFuture<Void> set(WebContext<AsyncSessionStore> context, String key, T value) {
        session.put(key, value);
        return CompletableFuture.completedFuture(null);
    }
}
