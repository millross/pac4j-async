package org.pac4j.async.core.logout;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.destruction.AsyncSessionDestructionStrategy;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Strategy interface for performing local logout. Contract is that it will return a completable future which will complete with
 * a list of all profiles which it logged out via the local profile manager (or an empty list if no local logout was performed)
 */
public interface AsyncLocalLogoutStrategy {
    <U extends CommonProfile, C extends AsyncWebContext> CompletableFuture<List<? extends U>>
    logout(final AsyncProfileManager<U, C> profileManager,
           final AsyncSessionDestructionStrategy sessionDestructionStrategy,
           final C webContext);
}
