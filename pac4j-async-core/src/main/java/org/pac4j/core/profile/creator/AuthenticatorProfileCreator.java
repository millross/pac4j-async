package org.pac4j.core.profile.creator;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;

/**
 *
 */
public class AuthenticatorProfileCreator<C extends Credentials, P extends CommonProfile> implements ProfileCreator<C, P> {

    public final static AuthenticatorProfileCreator INSTANCE = new AuthenticatorProfileCreator<>();

    @Override
    public P create(C credentials, WebContext<?> context) throws HttpAction {
        return (P) credentials.getUserProfile();
    }
}
