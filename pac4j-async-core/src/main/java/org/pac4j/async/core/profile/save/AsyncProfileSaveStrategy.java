package org.pac4j.async.core.profile.save;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Strategy interface to represent saving of profiles. Given a list of suppliers of boolean futures, where any
 * true value represents a saved profile,
 */
public interface AsyncProfileSaveStrategy {
    <T extends CommonProfile, C extends AsyncWebContext>CompletableFuture<Boolean> saveProfile(final AsyncProfileManager<T, C> manager,
                                                                                               final Function<T, Boolean> saveToSession,
                                                                                               T profile);
    CompletableFuture<Boolean> combineResults(final List<Supplier<CompletableFuture<Boolean>>> saveFutureSuppliers);
}
