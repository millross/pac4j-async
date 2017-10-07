package org.pac4j.core.logout;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.redirect.RedirectAction;

/**
 *
 */
public class NoLogoutActionBuilder <U extends CommonProfile> implements LogoutActionBuilder<U> {

    public static final NoLogoutActionBuilder INSTANCE = new NoLogoutActionBuilder<>();

    @Override
    public RedirectAction getLogoutAction(WebContext<?> context, U currentProfile, String targetUrl) {
        return null;
    }
}
