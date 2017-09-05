package org.pac4j.async.core.session.destruction;

import org.pac4j.async.core.context.AsyncWebContext;

import java.util.concurrent.CompletableFuture;

/**
 * Strategy interface relating to session destruction.
 */
public interface AsyncSessionDestructionStrategy {
   CompletableFuture<Void> attemptSessionDestructionFor(AsyncWebContext webContext);
}
