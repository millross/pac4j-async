package org.pac4j.async.vertx;

import org.pac4j.async.core.profile.AsyncProfileManager;
import org.pac4j.async.vertx.auth.Pac4jUser;
import org.pac4j.async.vertx.context.VertxAsyncWebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
public class VertxAsyncProfileManager extends AsyncProfileManager {
    private static final String SESSION_USER_HOLDER_KEY = "__vertx.userHolder";
    private final VertxAsyncWebContext vertxWebContext;

    public VertxAsyncProfileManager(VertxAsyncWebContext context) {
        super(context);
        this.vertxWebContext = context;
    }

    protected CompletableFuture<LinkedHashMap<String, CommonProfile>> retrieveAll(boolean readFromSession) {
        LinkedHashMap<String, CommonProfile> profiles = new LinkedHashMap();
        Pac4jUser user = this.vertxWebContext.getVertxUser();
        Optional.ofNullable(user).map(Pac4jUser::pac4jUserProfiles).ifPresent(profiles::putAll);
        return CompletableFuture.completedFuture(profiles);
    }

    public CompletableFuture<Void> remove(boolean removeFromSession) {
        this.vertxWebContext.removeVertxUser();
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> save(boolean saveInSession, CommonProfile profile, boolean multiProfile) {
        String clientName = this.retrieveClientName(profile);
        Pac4jUser vertxUser = Optional.ofNullable(this.vertxWebContext.getVertxUser()).orElse(new Pac4jUser());
        vertxUser.setUserProfile(clientName, profile, multiProfile);
        this.vertxWebContext.setVertxUser(vertxUser);
        return CompletableFuture.completedFuture(null);
    }

}
