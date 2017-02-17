package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 * Implementation of a profile authorizer where we know i/o will be non-blocking (i.e where we can use either an
 * AsyncWebContext or a sync WebContext).
 */
public abstract class NonBlockingProfileAuthorizer<U extends CommonProfile> implements Authorizer<WebContextBase<?>, U> {
    /**
     * If all profiles are authorized.
     *
     * @param context the web context
     * @param profiles the user profiles
     * @return whether all profiles are authorized
     * @throws HttpAction an extra HTTP action
     */
    public boolean isAllAuthorized(final WebContextBase<?> context, final List<U> profiles) throws HttpAction {
        for (final U profile : profiles) {
            if (!isProfileAuthorized(context, profile)) {
                return handleError(context);
            }
        }
        return true;
    }

    /**
     * If any of the profiles is authorized.
     *
     * @param context the web context
     * @param profiles the user profiles
     * @return whether any of the profiles is authorized
     * @throws HttpAction an extra HTTP action
     */
    public boolean isAnyAuthorized(final WebContextBase<?> context, final List<U> profiles) throws HttpAction {
        for (final U profile : profiles) {
            if (isProfileAuthorized(context, profile)) {
                return true;
            }
        }
        return handleError(context);
    }

    /**
     * Whether a specific profile is authorized.
     *
     * @param context the web context
     * @param profile the user profile
     * @return whether a specific profile is authorized
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected abstract boolean isProfileAuthorized(WebContextBase<?> context, U profile) throws HttpAction;

    /**
     * Handle the error.
     *
     * @param context the web context
     * @return <code>false</code>
     * @throws HttpAction whether an additional HTTP action is required
     */
    protected boolean handleError(final WebContextBase<?> context) throws HttpAction {
        return false;
    }
}
