package org.pac4j.async.core.logout;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.core.session.destruction.AsyncSessionDestructionStrategy;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Enumeration for stock types of local logout
 */
public enum AsyncLocalLogout implements AsyncLocalLogoutStrategy{


    ALWAYS_LOGOUT {
        @Override
        protected boolean shouldPerformLogout(final List<? extends CommonProfile> profiles) {
            return true;
        }
    },
    PROFILE_PRESENCE_DEPENDENT {
        @Override
        protected boolean shouldPerformLogout(final List<? extends CommonProfile> profiles) {
            return profiles.size() > 1;
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(AsyncLocalLogout.class);

    protected abstract boolean shouldPerformLogout(final List<? extends CommonProfile> profiles);

    @Override
    public <U extends CommonProfile, C extends AsyncWebContext> CompletableFuture<List<? extends U>> logout(AsyncProfileManager<U, C> profileManager, AsyncSessionDestructionStrategy sessionDestructionStrategy, C webContext) {
        return profileManager.getAll(true)
                .thenCompose(profiles -> {
                    if (shouldPerformLogout(profiles)) {
                        LOG.debug("Performing application logout");
                        return profileManager.logout()
                                .thenCompose(v -> sessionDestructionStrategy.attemptSessionDestructionFor(webContext))
                                .thenApply(v -> profiles);
                    } else {
                        return CompletableFuture.completedFuture(profiles);
                    }
                });

    }

}
