package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class IsAuthenticatedAuthorizer<U extends CommonProfile> extends AbstractCheckAuthenticationAuthorizer<WebContext<?>, U> {
    @Override
    public Boolean isAuthorized(WebContext<?> context, List<U> profiles) throws HttpAction {
        return isAnyAuthorized(context, profiles);
    }

    @Override
    protected String getErrorMessage() {
        return "user should be authenticated";
    }

    @Override
    protected Boolean isProfileAuthorized(WebContext<?> context, U profile) throws HttpAction {
        return profile != null && !(profile instanceof AnonymousProfile);
    }
}
