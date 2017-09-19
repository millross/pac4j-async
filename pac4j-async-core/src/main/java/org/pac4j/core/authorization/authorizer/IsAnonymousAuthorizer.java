package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class IsAnonymousAuthorizer<U extends CommonProfile> extends AbstractCheckAuthenticationAuthorizer<WebContext<?>, U> {

    public IsAnonymousAuthorizer() {}

    public IsAnonymousAuthorizer(final String redirectionUrl) {
        super(redirectionUrl);
    }

    @Override
    public Boolean isAuthorized(WebContext<?> context, List<U> profiles) throws HttpAction {
        return isAllAuthorized(context, profiles);
    }

    @Override
    protected String getErrorMessage() {
        return "user should be anonymous";
    }

    @Override
    protected Boolean isProfileAuthorized(WebContext<?> context, U profile) throws HttpAction {
        return profile == null || profile instanceof AnonymousProfile;
    }
}
