package org.pac4j.async.core.profile;

import org.pac4j.async.core.context.AsyncWebContext;
import org.pac4j.core.authorization.authorizer.Authorizer;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.pac4j.core.context.Pac4jConstants.USER_PROFILES;

/**
 * This class is a generic way to manage the current user profile(s), i.e. the one(s) of the current authenticated user.
 * This version allows for asynchronous session storage
 */
public class AsyncProfileManager<U extends CommonProfile, C extends AsyncWebContext> {
    private final Authorizer<WebContextBase<?>, U> IS_AUTHENTICATED_AUTHORIZER = new IsAuthenticatedAuthorizer<>();

    protected final AsyncWebContext context;

    public AsyncProfileManager(final C context) {
        this.context = context;
    }

    /**
     * Retrieve the first user profile if it exists, ignoring any {@link AnonymousProfile} if possible.
     *
     * @param readFromSession if the user profile must be read from session
     * @return the user profile
     */
    public CompletableFuture<Optional<U>> get(final boolean readFromSession) {
        final CompletableFuture<LinkedHashMap<String, U>> allProfilesFuture = retrieveAll(readFromSession);
        return allProfilesFuture.thenApply(ProfileHelper::flatIntoOneProfile);
    }

    /**
     * Retrieve all user profiles.
     *
     * @param readFromSession if the user profiles must be read from session
     * @return the user profiles
     */
    public CompletableFuture<List<U>> getAll(final boolean readFromSession) {
        final CompletableFuture<LinkedHashMap<String, U>> profilesFuture = retrieveAll(readFromSession);
        return profilesFuture.thenApply(ProfileHelper::flatIntoAProfileList);
    }

    /**
     * Remove the current user profile(s).
     *
     * @param removeFromSession if the user profile(s) must be removed from session
     */
    public CompletableFuture<Void> remove(final boolean removeFromSession) {

        this.context.setRequestAttribute(USER_PROFILES, new LinkedHashMap<String, U>());

        if (removeFromSession) {
            return this.context.setSessionAttribute(USER_PROFILES, new LinkedHashMap<String, U>());
        } else {
            // We've done all we need to
            return completedFuture(null);
        }
    }

    /**
     * Save the given user profile (replace the current one if multi profiles are not supported, add it otherwise).
     *
     * @param saveInSession if the user profile must be saved in session
     * @param profile a given user profile
     * @param multiProfile whether multiple profiles are supported
     */
    public CompletableFuture<Void> save(final boolean saveInSession, final U profile, final boolean multiProfile) {

        final String clientName = retrieveClientName(profile);

        final CompletableFuture<LinkedHashMap<String, U>> profilesFuture;
        if (multiProfile) {
            profilesFuture = retrieveAll(saveInSession).thenApply(profiles -> {
                profiles.remove(clientName);
                return profiles;
            });
        } else {
            profilesFuture = completedFuture(new LinkedHashMap<String, U>());
        }

        final CompletableFuture<LinkedHashMap<String, U>> updatedProfilesFuture = profilesFuture.thenApply(profiles -> {
            profiles.put(clientName, profile);
            return profiles;
        });

        return updatedProfilesFuture.thenCompose(profiles -> {
            CompletableFuture<Void> saveFuture = (saveInSession ?
                    this.context.setSessionAttribute(USER_PROFILES, profiles) :
                    completedFuture(null))
                        .thenAccept(v -> this.context.setRequestAttribute(USER_PROFILES, profiles));
                    return saveFuture;
        });

    }

    /**
     * Perform a logout by removing the current user profile(s).
     */
    public CompletableFuture<Void> logout() {
        return remove(true);
    }

    /**
     * Tests if the current user is authenticated (meaning a user profile exists which is not an {@link AnonymousProfile}).
     *
     * @return whether the current user is authenticated
     */
    public CompletableFuture<Boolean> isAuthenticated() {
        return getAll(true).thenApply(l -> {
            try {
                return IS_AUTHENTICATED_AUTHORIZER.isAuthorized(null, l);
            } catch (HttpAction httpAction) {
                throw new TechnicalException(httpAction);
            }
        });
    }


    /**
     * Retrieve the map of profiles from the session or the request.
     *
     * @param readFromSession if the user profiles must be read from session
     * @return the map of profiles
     */
    protected CompletableFuture<LinkedHashMap<String, U>> retrieveAll(final boolean readFromSession) {
        final LinkedHashMap<String, U> profiles = new LinkedHashMap<>();
        final Object request = this.context.getRequestAttribute(USER_PROFILES);
        if (request != null) {
            if  (request instanceof LinkedHashMap) {
                profiles.putAll((LinkedHashMap<String, U>) request);
            }
            if (request instanceof CommonProfile) {
                profiles.put(retrieveClientName((U) request), (U) request);
            }
        }
        if (readFromSession) {
            final CompletableFuture<Object> sessionAttributeFuture = this.context.getSessionAttribute(USER_PROFILES);
            return sessionAttributeFuture.thenCompose(sessionAttribute -> {
                final CompletableFuture<LinkedHashMap<String, U>> future = new CompletableFuture<>();
                this.context.getExecutionContext().runOnContext(() -> {
                    if  (sessionAttribute instanceof LinkedHashMap) {
                        profiles.putAll((LinkedHashMap<String, U>) sessionAttribute);
                    }
                    if (sessionAttribute instanceof CommonProfile) {
                        profiles.put(retrieveClientName((U) sessionAttribute), (U) sessionAttribute);
                    }
                    future.complete(profiles);
                });
                return future;
            });

        } else {
            return completedFuture(profiles);
        }
    }

    protected String retrieveClientName(final U profile) {
        String clientName = profile.getClientName();
        if (clientName == null) {
            clientName = "DEFAULT";
        }
        return clientName;
    }


}
