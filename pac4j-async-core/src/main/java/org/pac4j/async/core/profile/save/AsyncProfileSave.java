package org.pac4j.async.core.profile.save;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.future.FutureUtils;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.pac4j.async.core.future.FutureUtils.shortCircuitedFuture;

/**
 * Async profile save strategy enumeration allowing for multiprofile true or false
 */
public enum AsyncProfileSave implements AsyncProfileSaveStrategy {

    SINGLE_PROFILE_SAVE(false) {

        @Override
        public CompletableFuture<Boolean> combineResults(List<Supplier<CompletableFuture<Boolean>>> saveFutureSuppliers) {
            return shortCircuitedFuture(saveFutureSuppliers.stream(), true);
        }
    },
    MULTI_PROFILE_SAVE(true) {
        @Override
        public CompletableFuture<Boolean> combineResults(List<Supplier<CompletableFuture<Boolean>>> saveFutureSuppliers) {
            return FutureUtils.allInSequence(saveFutureSuppliers.stream());
//            final List<CompletableFuture<Boolean>> futureList = saveFutureSuppliers.stream()
//                    .map(s -> s.get()).collect(Collectors.toList());
//            return combineFuturesToList(futureList).thenApply(l ->
//                    l.stream().filter(b -> b == true).findFirst().orElse(false));
        }
    };


    private boolean multiProfile;

    AsyncProfileSave(final boolean multiProfile) {
        this.multiProfile = multiProfile;
    }

    private boolean isMultiProfile() { return multiProfile; }

    @Override
    public <T extends CommonProfile, C extends AsyncWebContext> CompletableFuture<Boolean> saveProfile(AsyncProfileManager<T, C> manager, Function<T, Boolean> determineSaveToSession, T profile) {
        return profile != null ? manager.save(determineSaveToSession.apply(profile), profile, multiProfile).thenApply(v -> Boolean.TRUE) :
        CompletableFuture.completedFuture(false);
    }
    
}
