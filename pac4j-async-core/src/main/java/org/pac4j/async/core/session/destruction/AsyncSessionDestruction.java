package org.pac4j.async.core.session.destruction;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.session.AsyncSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Enumeration to represent the session destruction behaviours we offer
 */
public enum AsyncSessionDestruction implements AsyncSessionDestructionStrategy {

    DO_NOT_DESTROY {


        @Override
        public CompletableFuture<Void> attemptSessionDestructionFor(AsyncWebContext webContext) {
            LOG.debug("No session destruction is configured");
            return CompletableFuture.completedFuture(null);
        }
    },
    DESTROY {
        @Override
        public CompletableFuture<Void> attemptSessionDestructionFor(AsyncWebContext webContext) {
            LOG.debug("Session destruction is configured");
            Optional<AsyncSessionStore> sessionStore = Optional.ofNullable(webContext.getSessionStore());

            final Optional<CompletableFuture<Void>> optionalFuture = sessionStore.map(store -> store.destroySession(webContext)
                    .thenApply(b -> {
                        if (!b) {
                            LOG.error("Unable to destroy the web session. The session store may not support this feature");
                        }
                        return null;
                    }));
            return optionalFuture.orElseGet(() -> {
                LOG.error("No session store available for this web context");
                return CompletableFuture.completedFuture(null);
            });

        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(AsyncSessionDestructionStrategy.class);

}
