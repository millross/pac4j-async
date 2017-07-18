package org.pac4j.async.core.session;

import org.pac4j.async.core.client.AsyncBaseClient;
import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.client.Clients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * Wrapper for the logic around session renewal besides the basic session store renewal code.
 */
public class AsyncSessionRenewer<C extends AsyncWebContext> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncSessionRenewer.class);

    protected CompletableFuture<Void> renewSession(final C context, final AsyncConfig<?, ?, C> config) {

        final AsyncSessionStore sessionStore = context.getSessionStore();

        if (sessionStore != null) {

            final CompletableFuture<String> oldSessionIdFuture = sessionStore.getOrCreateSessionId(context);
            final CompletableFuture<Boolean> renewed = sessionStore.renewSession(context);
            CompletableFuture<Void> result = renewed.thenCompose(b -> {
               if (b) {
                   final CompletableFuture<String> newSessionIdFuture = sessionStore.getOrCreateSessionId(context);
                   return oldSessionIdFuture.thenCombine(newSessionIdFuture, (oldSessionId, newSessionId) -> {
                       logger.debug("Renewing session: {} -> {}", oldSessionId, newSessionId);
                       final Clients<AsyncClient<?, ?>, ?> clients = config.getClients();
                       if (clients != null) {
                           clients.getClients().stream()
                                   .map(ac -> (AsyncBaseClient<?, ?>) ac)
                                   .forEach(bc -> bc.notifySessionRenewal(oldSessionId, context));
                       }
                       return null;
                   });
               } else {
                   logger.error("Unable to renew the session. The session store may not support this feature");
                   return CompletableFuture.completedFuture(null);
               }
            });
            return result;

        } else {
            logger.error("No session store available for this web context");
            return completedFuture(null);
        }
    }

}
