package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class IsFullyAuthenticatedAuthorizer<U extends CommonProfile> extends AbstractCheckAuthenticationAuthorizer<WebContextBase<?>, U> {
    @Override
    public Boolean isAuthorized(final WebContextBase<?> context, final List<U> profiles) throws HttpAction {
        return isAnyAuthorized(context, profiles);
    }

    @Override
    protected String getErrorMessage() {
        return "user should be fully authenticated";
    }

    @Override
    protected Boolean isProfileAuthorized(WebContextBase<?> context, U profile) throws HttpAction {
        return profile != null && !(profile instanceof AnonymousProfile) && !profile.isRemembered();
    }
}
