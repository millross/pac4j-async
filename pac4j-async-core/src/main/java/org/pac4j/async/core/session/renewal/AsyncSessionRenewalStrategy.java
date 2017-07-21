package org.pac4j.async.core.session.renewal;

import org.pac4j.async.core.config.AsyncConfig;
import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.concurrent.CompletableFuture;

/**
 * Interface to reflect session renewal in async systems - we use an implementation of this strategy as a mechanism
 * for renewing sessions (including any logical checks around the edges)
 */
public interface AsyncSessionRenewalStrategy {
    <R, U extends CommonProfile, C extends AsyncWebContext>CompletableFuture<Void> renewSession(C context, AsyncConfig<R, U, C> config);
}
