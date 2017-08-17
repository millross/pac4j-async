package org.pac4j.async.core.authenticate.failure.recorder;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;

import java.util.concurrent.CompletableFuture;

import static org.pac4j.core.client.IndirectClient.ATTEMPTED_AUTHENTICATION_SUFFIX;

/**
 * Enum for strategies to record authentication failure
 */
public enum RecordFailedAuth implements RecordFailedAuthenticationStrategy {

    DO_NOT_RECORD {
        @Override
        public CompletableFuture<Credentials> recordFailedAuthentication(final AsyncClient client, final Credentials credentials, final AsyncWebContext webContext) {
            return CompletableFuture.completedFuture(credentials);
        }

        @Override
        public CompletableFuture<Void> clearFailedAuthentication(AsyncClient client, AsyncWebContext webContext) {
            return CompletableFuture.completedFuture(null);
        }

    },
    RECORD_IN_SESSION {
        @Override
        public CompletableFuture<Credentials> recordFailedAuthentication(final AsyncClient client, final Credentials credentials, final AsyncWebContext webContext) {
            return webContext.setSessionAttribute(client.getName() + ATTEMPTED_AUTHENTICATION_SUFFIX, "true")
                    .thenApply(v -> credentials);
        }

        @Override
        public CompletableFuture<Void> clearFailedAuthentication(AsyncClient client, AsyncWebContext webContext) {
            return webContext.setSessionAttribute(client.getName() + ATTEMPTED_AUTHENTICATION_SUFFIX, "");        }
    }

}
