package org.pac4j.core.authorization.authorizer;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.AnonymousProfile;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

/**
 *
 */
public class IsRememberedAuthorizer<U extends CommonProfile> extends AbstractCheckAuthenticationAuthorizer<WebContext<?>, U> {

    public IsRememberedAuthorizer() {}

    public IsRememberedAuthorizer(final String redirectionUrl) {
        super(redirectionUrl);
    }

    @Override
    public Boolean isAuthorized(final WebContext<?> context, final List<U> profiles) throws HttpAction {
        return isAnyAuthorized(context, profiles);
    }

    @Override
    public Boolean isProfileAuthorized(final WebContext<?> context, final U profile) throws HttpAction {
        return profile != null && !(profile instanceof AnonymousProfile) && profile.isRemembered();
    }

    @Override
    protected String getErrorMessage() {
        return "user should be remembered";
    }
}
