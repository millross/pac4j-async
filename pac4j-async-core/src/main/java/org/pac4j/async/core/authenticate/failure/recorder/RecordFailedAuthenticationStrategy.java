package org.pac4j.async.core.authenticate.failure.recorder;

import org.pac4j.async.core.client.AsyncClient;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.credentials.Credentials;

import java.util.concurrent.CompletableFuture;

/**
 * Interface governing how to record a failed authentication. Direct clients do not need to record, indirect clients
 * do need to record the fact that they have attempted and failed. Therefore by implementing a DO_NOT_RECORD and a
 * RECORD_IN_SESSION strategy more client code can be made common.
 */
public interface RecordFailedAuthenticationStrategy {
    <C extends Credentials> CompletableFuture<C> recordFailedAuthentication(final AsyncClient client, final C credentials, final AsyncWebContext webContext);
    <C extends Credentials> CompletableFuture<C> clearFailedAuthentication(final AsyncClient client, final AsyncWebContext webContext);
}
