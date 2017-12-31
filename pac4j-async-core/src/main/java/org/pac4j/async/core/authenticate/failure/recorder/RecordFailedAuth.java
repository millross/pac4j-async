package org.pac4j.async.core.authenticate.failure.recorder;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.util.CommonHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Enum for strategies to record authentication failure
 */
public enum RecordFailedAuth implements RecordFailedAuthenticationStrategy {

    DO_NOT_RECORD {
        @Override
        public <C extends Credentials> CompletableFuture<C> recordFailedAuthentication(final AsyncClient client, final C credentials, final AsyncWebContext webContext) {
            return CompletableFuture.completedFuture(credentials);
        }

        @Override
        public <C extends Credentials> CompletableFuture<C> clearFailedAuthentication(AsyncClient client, final C credentials, AsyncWebContext webContext) {
            return CompletableFuture.completedFuture(credentials);
        }

        @Override
        public CompletableFuture<Boolean> isFailedAuthenticationPresent(AsyncClient client, AsyncWebContext webContext) {
            return CompletableFuture.completedFuture(false);
        }


    },
    RECORD_IN_SESSION {
        @Override
        public <C extends Credentials> CompletableFuture<C> recordFailedAuthentication(final AsyncClient client, final C credentials, final AsyncWebContext webContext) {
            return webContext.getSessionStore().set(webContext, client.getName() + ATTEMPTED_AUTHENTICATION_SUFFIX, "true")
                    .thenApply(v -> credentials);
        }

        @Override
        public <C extends Credentials> CompletableFuture<C> clearFailedAuthentication(AsyncClient client, final C credentials, AsyncWebContext webContext) {
            return webContext.getSessionStore().set(webContext, client.getName() + ATTEMPTED_AUTHENTICATION_SUFFIX, "")
                    .thenApply(v -> credentials);
        }

        @Override
        public CompletableFuture<Boolean> isFailedAuthenticationPresent(AsyncClient client, AsyncWebContext webContext) {
            return webContext.getSessionStore().<String>get(webContext, client.getName() + ATTEMPTED_AUTHENTICATION_SUFFIX)
                    .thenApply(CommonHelper::isNotBlank);
        }
    }

}
